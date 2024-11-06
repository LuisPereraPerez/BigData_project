import csv
import re
from pymongo import MongoClient
from unidecode import unidecode
from simplemma import lemmatize
from termcolor import colored

# MongoDB connection configuration
client = MongoClient('mongodb://localhost:27017/')
db = client['SEARCH_ENGINE']
words_collection = db['WORDS']

# Load metadata from the CSV file to get the book title
def get_book_title_from_csv(book_id):
    with open('datalake/metadata.csv', mode='r', encoding='utf-8') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            if row['ID'] == book_id:
                return row['Title']
    return f"BookID_{book_id}"  # Default title if not found

# Read the lines of a book and search for those containing the search word (case-insensitive)
def read_lines_with_word(book_file, paragraphs, search_word):
    try:
        with open(f"datalake/books/{book_file}", encoding='utf-8') as file:
            lines = file.readlines()

        result = []
        paragraph_count = 0
        current_paragraph_lines = []

        search_word_lower = search_word.lower()  # Convert the word to lowercase for case-insensitive search

        for i, line in enumerate(lines):
            # If the line is empty, the paragraph has ended
            if line.strip() == "":
                if current_paragraph_lines:
                    # Only if the current paragraph is in the selected ones
                    if paragraph_count in paragraphs:
                        for current_line in current_paragraph_lines:
                            # Case-insensitive search
                            if search_word_lower in current_line.lower():
                                # Highlight the word in magenta while maintaining the original case
                                highlighted_line = re.sub(f"(?i)({re.escape(search_word)})", colored(r"\1", 'magenta'), current_line)
                                result.append(f"Line {i + 1}: {highlighted_line.strip()}")
                    current_paragraph_lines = []  # Reset the paragraph
                    paragraph_count += 1  # Increase the paragraph counter
            else:
                # Add the current line to the paragraph
                current_paragraph_lines.append(line)

        return result

    except FileNotFoundError:
        print(f"The file '{book_file}' was not found.")
        return []

# Extract the paragraph number from MongoDB keys
def extract_paragraph_numbers(paragraph_keys):
    paragraph_numbers = []
    for key in paragraph_keys:
        match = re.search(r'\d+$', key)  # Look for numbers at the end of the string
        if match:
            paragraph_numbers.append(int(match.group()))
        else:
            print(f"Skipping invalid paragraph key: {key}")
    return paragraph_numbers

# Search for a word in MongoDB and print the lines that contain it
def search_word(word, lang='en'):
    word = unidecode(word.lower()).strip()  # Normalize and clean the word
    word = lemmatize(word, lang)  # Lemmatize the word
    
    # Query MongoDB to search for the word
    result = words_collection.find_one({'word': word})
    
    if result:
        # Print the total occurrences
        print(f"\nThe word '{word}' appears in the following books:")

        # Iterate over the books where the word appears
        for book_file, book_data in result['books'].items():
            book_id = book_file.split('.')[0]  # Extract the book ID
            book_title = get_book_title_from_csv(book_id)  # Get the title from the CSV
            print(f"\nBook Title: {book_title}")
            
            # Extract the paragraph numbers
            paragraphs = extract_paragraph_numbers(book_data.keys())
            lines_with_word = read_lines_with_word(book_file, paragraphs, word)
            for line in lines_with_word:
                print(line)
    else:
        print(f"\nThe word '{word}' was not found in the database.\n")

# Main function
def main():
    word_to_search = input("Enter the word you want to search: ")
    search_word(word_to_search)

# Execute the program
if __name__ == "__main__":
    main()
