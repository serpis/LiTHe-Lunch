# -*- coding: utf-8 -*-

from urllib.request import urlopen
import re
from datetime import date, timedelta
import time
import json
import sys
import xml.sax
import xml.sax.handler


def slurp(url):
	data = urlopen(url).read()
	try:
		s = data.decode("utf-8")
	except:
		s = data.decode("iso-8859-15")
	return re.sub(r"[\r\n]+", "", s)

class ParseError:
	def __init__(self, msg):
		self.msg = msg

def resolve_weekday_offset(weekday):
	# Monday = 1. the reason is that python likes
	# weeks starting on Sunday, with Sunday = 0
	if weekday[0] == 'M':
		return 1
	elif weekday[0:2] == 'Ti':
		return 2
	elif weekday[0] == 'O':
		return 3
	elif weekday[0:2] == 'To':
		return 4
	elif weekday[0] == 'F':
		return 5
	else:
		raise ParseError("couldn't resolve weekday offset for: " + weekday)

# function that transforms week, weekday touple to date object (Monday = 1)
def resolve_date(year, week, weekday):
	t = time.strptime("%04d %02d %d" % (year, week, weekday), "%Y %W %w")
	d = date(t.tm_year, t.tm_mon, t.tm_mday)
	return d

def sanitize(s):
	# a few steps of sanitation..
	s = re.sub("&nbsp;", " ", s)
	s = re.sub("&amp;", "&", s)
	s = re.sub("\<[^>]*\>", "", s)
	# ..and one for teh lulz
	s = re.sub("kulpotatis", "kul potatis", s)
	s = s.strip()
	return s

def json_date(d):
	return { "year": d.year, "month": d.month, "day": d.day }

def blamesen_menu():
	data = slurp("http://www.blamesen.se/default.asp?HeadPage=464&Lunchmeny")
	(data, week) = re.search(r"(<span class=\"header_XL\">Lunchmeny&nbsp;&nbsp;-&nbsp;&nbsp;Vecka (\d+).*?<div class=\"clear\">)", data).groups()

	# year is implicitly the same as current year
	year = date.today().year
	menu = []
	for weekday, content in re.findall(r"<span class=\"header_XL\">([^<]+?dag)</span>.*?<td (?:class=\"(?:font14|h1_colored)\" )?(?:valign=\"top\" )?width=\"70%\">(.+?)</tr>", data):
		dishes = []
		for dish_name in re.findall(r">\s?(.+?)<", content):
			dish_name = sanitize(dish_name)
			if len(dish_name) > 0:
				dishes.append({ "name": dish_name, "price": "dummy"})
		weekday_offset = resolve_weekday_offset(weekday)
		menu.append({ "date": resolve_date(year, int(week), weekday_offset), "dishes": dishes })
	return menu

def karallen_menu():
	# first, we need some detective work to find lunch menu url
	start_data = slurp("http://www.cgnordic.com/sv/Eurest-Sverige/Restauranger/Restaurang-Karallen-Linkopings-universitet/")
	base_url = "http://www.cgnordic.com"
	#extra_url = re.search(r"\"([^\"]+)\">\s+Lunchmeny", start_data).group(1)
	extra_url = re.search(r"\"([^\"]+)\">Dagens lunch", start_data).group(1)
	menu_url = base_url + extra_url

	data = slurp(menu_url)
	week = int(re.search("Lunchmeny v.(\S+)", data).group(1))

	# cut out roughly the relevant part
	data = re.search(r"lkommen&(.+i?)Pris dagens", data).group(1)

	# year is implicitly the same as current year
	year = date.today().year
	menu = []
	for weekday, content in re.findall(r"<strong>(....?dag)(.*?)(?=<strong|$)", data):
		dishes = []
		for dish_name in re.findall(r">([^>]+?)(?:</p>)?</td></tr>", content):
			dish_name = sanitize(dish_name)
            # remove any mention about Cellskapet!
			dish_name = re.sub("\([^)]*Cellskapet[^)]*\)", "", dish_name)
			if len(dish_name) > 0:
				dishes.append({ "name": dish_name, "price": "dummy"})
		weekday_offset = resolve_weekday_offset(weekday)
		menu.append({ "date": resolve_date(year, int(week), weekday_offset), "dishes": dishes })

	return menu
	
def zenit_menu():
	class ZenitFeedParser(xml.sax.handler.ContentHandler):
		def __init__(self):
			self.elementStack = []

			self.title = ""
			self.summary = ""
			self.publisheddate = ""

			self.titles = []
			self.summaries = []
			self.publisheddates = []

		def startElement(self, name, attrs):
			self.elementStack.append(name)

		def endElement(self, name):
			self.elementStack.pop()

			if name == "title" and "entry" in self.elementStack:
				self.titles.append(self.title)
				self.title = ""
			elif name == "published" and "entry" in self.elementStack:
				self.publisheddates.append(self.publisheddate)
				self.publisheddate = ""
			elif name == "summary":
				self.summaries.append(self.summary)
				self.summary = ""

		def characters(self, content):
			if self.elementStack[-1] == "title" and "entry" in self.elementStack:
				self.title += content
			elif self.elementStack[-1] == "published" and "entry" in self.elementStack:
				self.publisheddate += content
			elif self.elementStack[-1] == "summary":
				self.summary += content

	p = ZenitFeedParser()
	xml.sax.parse("http://www.hors.se/dagens-lunch/feed?restaurant=zenit", p)

	# every day follows the same pattern: <h2>weekday</h2><ul><li>dish1</li><li>dish2</li></ul>
	class DayMenuParser(xml.sax.handler.ContentHandler):
		def __init__(self):
			self.elementStack = []

			self.currentDay = ""

			self.dishes = []
			self.menu = []

			self.weekdayOffset = 0

		def startElement(self, name, attrs):
			self.elementStack.append(name)

		def endElement(self, name):
			self.elementStack.pop()

			# end of day menu!
			if name == "ul":
				self.menu.append({ "date": resolve_date(year, week, self.weekdayOffset), "dishes": self.dishes })
				self.dishes = []

		def characters(self, content):
			if self.elementStack[-1] == "h2":
				self.weekdayOffset = resolve_weekday_offset(content)
			elif self.elementStack[-1] == "li":
				dish_name = sanitize(content)
				dish_name = re.sub(r"^Det saknas.*", "", dish_name)
				dish_name = re.sub(r" *¤", "", dish_name)
				if len(dish_name) > 0:
					self.dishes.append({ "name": dish_name, "price": "dummy" })


	dmp = DayMenuParser()

	for i in range(len(p.titles)):
		year, month, day = [int(x) for x in re.search(r"^(\d{4})\-(\d{2})\-(\d{2})", p.publisheddates[i]).group(1, 2, 3)]
		week = int(date(year, month, day).strftime("%V"))
		
		for chunk in re.findall(r"<.+?>|[^<]+", p.summaries[i]):
			if chunk[0] == "<":
				name = re.search(r"<\/?([^>]+)>", chunk).group(1)
				if chunk[1] == "/":
					dmp.endElement(name)
				else:
					dmp.startElement(name, None)
			else:
				dmp.characters(chunk)

	return dmp.menu



# stolen from http://www.peterbe.com/plog/uniqifiers-benchmark
def uniqify(seq, idfun=None): 
    # order preserving
	if idfun is None:
		def idfun(x): return x
	seen = {}
	result = []
	for item in seq:
		marker = idfun(item)
		# in old Python versions:
		# if seen.has_key(marker)
		# but in new ones:
		if marker in seen: continue
		seen[marker] = 1
		result.append(item)
	return result


def merge_menus(menus):
	# start by getting all unique dates in order
	dates = []
	for menu in menus:
		for daily_menu in menu["menu"]:
			date = daily_menu["date"]
			dates.append(date)
	dates = uniqify(dates)
	dates.sort()

	# then create a menu for each day containing the dishes for all restaurants
	processed_menus = []
	for date in dates:
		ms = []
		for menu in menus:
			restaurant_name = menu["name"]
			for daily_menu in menu["menu"]:
				d = daily_menu["date"]
				if d == date:
					ms.append({ "name": restaurant_name, "dishes": daily_menu["dishes"]})
		processed_menus.append({"date": date, "menus": ms})

	return processed_menus

def jsonify_dates(menus):
	processed = []
	for menu in menus:
		processed.append({"date": json_date(menu["date"]), "menus": menu["menus"]})
	return processed



def render_html(menus):
	class Element:
		def __init__(self, tag):
			self.tag = tag
			self.children = []
			self.attributes = []

		def add(self, child):
			self.children.append(child)

		def render(self):
			head = "<" + self.tag
			for a in self.attributes:
				head += " %s=%s" % a
			head += ">"
			tail = "</" + self.tag + ">"

			s = ""
			for child in self.children:
				if isinstance(child, Element):
					s += child.render()
				else:
					s += child

			return head + s + tail

	def string_element(tag, s):
		e = Element(tag)
		e.add(s)
		return e

	def friendly_date_string(date):
		weekday = date.weekday()
		weekdays = ["Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag"]
		
		return weekdays[weekday] + " " + date.strftime("%Y-%m-%d")


	root = Element("html")
	head = Element("head")
	body = Element("body")

	head.add(string_element("title", "LiTHe Lunch"))
	head.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />")
	head.add("<script src=\"viewer.js\"></script>")
	root.add(head)
	root.add(body)

	body.attributes.append(("onload", "\"hideStuff();\""))

	for day_menu in menus:
		dm = Element("div")
		header = Element("a")
		header.attributes.append(("id", '"%s"' % friendly_date_string(day_menu["date"])))
		header.add(string_element("h1", friendly_date_string(day_menu["date"])))
		dm.add(header)
		for restaurant_menu in day_menu["menus"]:
			dm.add(string_element("h2", restaurant_menu["name"]))
			dish_list = Element("ul")
			for dish in restaurant_menu["dishes"]:
				dish_list.add(string_element("li", dish["name"]))
			dm.add(dish_list)

		body.add(dm)

	return root.render()

output_html = len(sys.argv) == 2 and sys.argv[1] == "html"


#restaurants = [("Blåmesen", blamesen_menu), ("Kårallen", karallen_menu), ("Zenit", zenit_menu)]
restaurants = [("Blåmesen", blamesen_menu), ("Zenit", zenit_menu)]
menus = []
for (name, menu_getter) in restaurants:
	menus.append({ "name": name, "menu": [x for x in menu_getter() if (date.today() - x["date"]).days < 7] })
if not output_html:
	today = date.today()
	this_monday = today - timedelta(today.weekday())
	menus.append({ "name": "Tips", "menu": [{ "date": this_monday, "dishes": [{ "name": "Du vet väl om att lunchmenyerna även finns på http://lunch.serp.se/menu.html", "price": "dummy" }] }] })
merged_menus = merge_menus(menus)

# print using utf-8. source: http://stackoverflow.com/questions/3597480/how-to-make-python-3-print-utf8
def print_utf8(text):
	sys.stdout.buffer.write(text.encode("utf-8"))

if output_html:
	print_utf8(render_html(merged_menus))
else:
	print_utf8(json.dumps(jsonify_dates(merged_menus)))


