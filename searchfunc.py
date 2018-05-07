#Global variable, to be handled by User Login (for the name) and database (for the universal ID of the tour object in the database)
id = 0
name = "Duy"

#This function reads a binary (currently in decimal) and returns the corresponding string
#This function is used for reading the binary representation of transportation method and days of the week
#For transportation method:
#	String length should be 2
#	First digit represents walking
#	Second digit represents car
#For days of the week:
#	String length should be 7
#	Each digit corresponds to a day of the week, from monday -> sunday
def readBinary(string, length):
	days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

	#Convert the decimal number into binary
	toString = str(bin(string))[2:]

	#Add leading zeros if needed
	if len(toString) < length:
		offsetString = ""
		for offset in range(0,length - len(toString)):
			offsetString += "0"
		toString = offsetString + toString

	#Returning the correct string based on the binary
	if length == 2:
		if toString == "10":
			return "Walk only"
		elif toString == "01":
			return "Car only"
		elif toString == "11":
			return "Both car and walk"
	elif length == 7:
		toReturn = ""
		for char_index in range(0, 7):
			if toString[char_index] == "1":
				toReturn += days[char_index] + ", "
		return toReturn[:-2]

	return "Error"

#Class definition for object Tour
#Each tour contains the member variables:
#	id 		- a unique id given by the database
#	name 		- the guide's name
#	location	- tour location (city)
#	transportation	- transportation method (will be stored in decimal int form)
#	day		- days of the week for the tour (will be stored in decimal int form)
#	duration	- length of tour
#	price		- pricing of tour
class Tour:
	#Constructor
	def __init__ (self, name, location, transportation, day, duration, price):
		global id
		self.id = id
		id += 1
		self.name = name
		self.location = location
		self.transportation = transportation
		self.day = day
		self.duration = duration
		self.price = price

	#toString()
	def __str__ (self):
		idString = "\nTour ID: " + str(self.id)
		nameString = "\nGuide: " + self.name
		locationString = "\nLocation: " + self.location
		transportString = "\nTransportation: " + readBinary(self.transportation, 2)
		dayString = "\nDay: " + readBinary(self.day, 7)
		durationString = "\nDuration: " + str(self.duration) + " hour(s)"
		priceString = "\nPrice: $" + str(self.price)
		return idString + nameString + locationString + transportString + dayString + durationString + priceString
	def changeLocation (self, newLocation):
		print "\nLocation updated: " + self.location + " -> " + newLocation
		self.location = newLocation
	def changeTransportation (self, newTransportation):
		print "\nTransportation updated: " + readBinary(self.transportation, 2) + " -> " + readBinary(newTransportation, 2)
		self.transportation = newTransportation
	def changeDay (self, newDay):
		print "\nDay updated: " + readBinary(self.day, 7) + " -> " + readBinary(newDay, 7)
		self.day = newDay
	def changeDuration (self, newDuration):
		print "\nDuration updated: " + str(self.duration) + " -> " + str(newDuration)
		self.duration = newDuration
	def changePrice (self, newPrice):
		print "\nPrice updated: " + str(self.price) + " -> " + str(newPrice)
		self.price = newPrice

#Hashtable containing all the current tours, representing the database
tour_list = {}


## DEBUG ##
#Hard-coded current available tours
newTour = Tour("Duy", "sd", int("11", base = 2), int("1010100", base = 2), 3, 30)
tour_list[newTour.id] = newTour
newTour = Tour("Duy", "sd", int("10", base = 2), int("1100000", base = 2), 5, 35)
tour_list[newTour.id] = newTour
## END DEBUG ##

location_list = []

#Main menu loop
while True:
	input = raw_input("\nOptions:\n1. New Tour\n2. Search Tour\n3. Update Tour\n4. Delete Tour\n5. Change user\n6. View tour\nChoice: ")

	#Create new tour
	if input == "1":
		print "\nInput details for new tour"
		
		#Get location
		location = raw_input("Tour location: ").lower()
		if not location in location_list:
			location_list.append(location)
		
		#Get transportation method, input is in binary form, convert it to decimal int before storing
		transportation = int(raw_input("Transportation (walk or car, binary): "), base = 2)

		#Get day of the week in which tour happen, input is in binary form, convert it to decimal int before storing
		day = int(raw_input("How often is this tour (days of week, binary): "), base = 2)

		#Get tour duration
		duration = int(raw_input("Length of tour in hours: "))

		#Get price
		price = int(raw_input("Price for the tour: "))

		#Create a new tour based on the inputs and add it to the database
		newTour = Tour(name, location, transportation, day, duration, price)
		tour_list[newTour.id] = newTour

		print "\n== Added a new tour =="
		print str(newTour)
		print "\n======================"

	#Search for a tour	
	elif input == "2":
		print "\nInput details to search for a tour"
		
		#Get location
		location = raw_input("Tour location: ").lower()

		#Get transportation method
		transportation = int(raw_input("Transportation (walk or car, binary, 00 means no preference): "), base = 2)

		#Get day of the week in which tour happen
		day = int(raw_input("When can you take the tour (days of week, binary, 0000000 means no preference): "), base = 2)

		#Get tour duration
		min_duration = int(raw_input("Min length: "))
		max_duration = int(raw_input("Max length: "))

		#Get price
		min_price = int(raw_input("Min price: "))
		max_price = int(raw_input("Max price: "))

		#Find all matching tours and add the ID into an array
		return_id = []
		for tour_id in tour_list:
			current_tour = tour_list[tour_id]
			if current_tour.location == location and \
					(transportation == 0 or current_tour.transportation & transportation) and \
					(day == 0 or current_tour.day & day) and \
					(min_duration == 0 or current_tour.duration > min_duration) and \
					(max_duration == 0 or current_tour.duration < max_duration) and \
					(min_price == 0 or current_tour.price > min_price) and \
					(max_price == 0 or current_tour.price < max_price):
						return_id.append(tour_id)

		#Print search result from the array of tour IDs
		if len(return_id) == 0:
			print "\nSorry we found no match"
		else:
			print "\nMatching tour(s):"
			for tour_id in return_id:
				print str(tour_list[tour_id])

	#Updating a tour
	elif input == "3":

		#List all tours under your name
		available_tour = []
		for elements in tour_list:
			if tour_list[elements].name == name:
				available_tour.append(elements)
				print str(tour_list[elements])

		#Get input for which tour to be changed
		while True: 
			input = raw_input("\nPick a tour to change (based on ID), enter -1 to cancel: ")
			if int(input) in available_tour or input == "-1":
				break
			else:
				print "\nInvalid tour ID"

		#Changing the chosen tour
		if input != "-1":
			current_tour = tour_list[int(input)]
			print "\n== Tour to be changed =="
			print str(current_tour)
			print "\n========================"
		
			#Get input for which field to be changed
			while True:	
				input = raw_input("\nOptions:\n1. Change location\n2. Change transportation (binary) \
						\n3. Change day (binary)\n4. Change duration\n5. Change price\n-1. Cancel\nChoice: ")
				if (1 <= int(input) and int(input) <= 5) or input == "-1":
					break
				print "Invalid input"
		
			#Get the new value for the chosen field
			if input != "-1":
				if input == "1":
					current_tour.changeLocation(raw_input("\nEnter new location: "))
				elif input == "2":
					current_tour.changeTransportation(int(raw_input("\nEnter new transportation (binary): "), base = 2))
				elif input == "3":
					current_tour.changeDay(int(raw_input("\nEnter new day (binary): "), base = 2))
				elif input == "4":
					current_tour.changeDuration(raw_input("\nEnter new duration: "))
				elif input == "5":
					current_tour.changePrice(raw_input("\nEnter new price: "))

	elif input == "4":
		#List all tours under your name
		available_tour = []
		for elements in tour_list:
			if tour_list[elements].name == name:
				available_tour.append(elements)
				print str(tour_list[elements])

		#Get input for which tour to be deleted
		while True: 
			input = raw_input("\nPick a tour to delete (based on ID), enter -1 to cancel: ")
			if int(input) in available_tour or input == "-1":
				break
			else:
				print "\nInvalid tour ID"

		#Confirm deletion
		if input != "-1":
			current_id = int(input)
			current_tour = tour_list[current_id]
			print "\n== Tour to be deleted =="
			print str(current_tour)
			print "\n========================"

			#Confirmation
			while True:	
				input = raw_input("\nOptions:\n1. Confirm\n-1. Cancel\nChoice: ")
				if input == "1" or input == "-1":
					break
				print "Invalid input"

			if input == "1":
				del tour_list[current_id]
				print "\nTour deleted"

	#Logout / login function, to change user. Currently placeholder
	elif input == "5":
		name = raw_input("\nInput new username: ")

	#List all tours under your name
	elif input == "6":
		for elements in tour_list:
			if tour_list[elements].name == name:
				print str(tour_list[elements])
		
	else:
		print "\nInvalid input"
	
	pause = raw_input("\nPress Enter to continue")
