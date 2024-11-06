import csv
import json
from unidecode import unidecode
from simplemma import lemmatize

# Function to get the book title from the metadata.csv file
def get_book_title(book_id):
    with open('datalake/metadata.csv', mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            if row['ID'] == str(book_id):
                return row['Title']
    return f"BookID_{book_id}"  # If the title is not found, return the BookID.

# Function to get the occurrences of a word in all the books where it appears
def get_word_occurrences_in_books(word):
    try:
        with open(f"datamart/reverse_indexes/{word[0].lower()}/{word}.json", 'r', encoding='utf-8') as json_file:
            existing_data = json.load(json_file)
            return existing_data['allocations']  # Returns a dictionary with the books and positions
    except FileNotFoundError:
        raise ValueError(f"The word '{word}' has not been indexed yet.")

# Function to read the lines of a specific book where the word appears
def read_paragraphs(book_id, occurrences, search_word):
    with open(f"datalake/books/{book_id}.txt", encoding='utf-8') as fp:
        result = []
        current_occurrence = 0
        occurrences.sort(key=lambda x: x[0])  # Ensure positions are in order
        for i, line in enumerate(fp, start=1):
            # If the current line is in the positions where the word appears
            while current_occurrence < len(occurrences) and i == occurrences[current_occurrence][0]:
                # Highlight the word in purple using ANSI escape codes
                highlighted_line = line.replace(search_word, f"\033[35m{search_word}\033[0m")
                result.append(f"Line {i}: {highlighted_line.strip()} (Position {occurrences[current_occurrence][1]})")
                current_occurrence += 1
                if current_occurrence >= len(occurrences):
                    break
    return result

# Main function to search the word in the inverted index and display the occurrences across all books
def search_word_across_books(word, lang='en'):
    word = unidecode(word.lower()).strip()  # Clean the word
    word = lemmatize(word, lang)  # Lemmatize the word

    try:
        occurrences_by_book = get_word_occurrences_in_books(word)  # Get the books and positions
        if occurrences_by_book:
            print(f"The word '{word}' appears in the following books:")
            for book_id, info in occurrences_by_book.items():
                title = get_book_title(book_id.split('_')[-1])  # Get the book title from metadata.csv
                valid_positions = [pos for pos in info['position'] if pos]
                if valid_positions:
                    print(f"\nBook Title: {title}")
                    paragraphs = read_paragraphs(book_id.split('_')[-1], valid_positions, word)
                    for paragraph in paragraphs:
                        print(paragraph)
        else:
            print(f"The word '{word}' was not found in any books.")
    except ValueError as e:
        print(e)

if __name__ == "__main__":
    word = input("Enter the word to search: ")
    search_word_across_books(word)
