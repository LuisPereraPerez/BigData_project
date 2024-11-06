import os
from pymongo import MongoClient
import re
from unidecode import unidecode
from simplemma import lemmatize

# Configure the MongoDB connection
client = MongoClient('mongodb://localhost:27017/')
db = client['SEARCH_ENGINE']  # Database name
words_collection = db['WORDS']  # Collection name for words
processed_books_collection = db['PROCESSED_BOOKS']  # Collection name for processed books

# Predefined pre-path
PRE_PATH = "datalake/books/"

# Clean and lemmatize words
def clean_word(word):
    # Remove unwanted characters and normalize the word
    word = re.sub(r'^_.*|_.*_|_.*$|[\d]+|[^\w\s]', '', word)  # Remove special characters and digits
    word = unidecode(word).strip('_').strip()  # Normalize the word and remove leading/trailing underscores and spaces
    return word if word else None  # Return None if the word is empty

def lemm_add(dictionary, word, lang='en'):
    if word:  # Ensure word is not None
        lemm = lemmatize(word, lang)
        if lemm != word:
            if lemm not in dictionary:
                dictionary[lemm] = {'allocations': {}, 'total': 0}
            if "allocations" in dictionary[word]:
                pop = dictionary.pop(word)
                for book_key, new_info in pop['allocations'].items():
                    if book_key in dictionary[lemm]['allocations']:
                        # Merge positions without duplicates
                        existing_positions = set(tuple(pos) for pos in dictionary[lemm]['allocations'][book_key]['position'])
                        existing_positions.update(tuple(pos) for pos in new_info['position'])
                        dictionary[lemm]['allocations'][book_key]['position'] = list(existing_positions)
                        # Update count based on unique positions
                        dictionary[lemm]['allocations'][book_key]['times'] = len(existing_positions)
                    else:
                        dictionary[lemm]['allocations'][book_key] = new_info
                # Update total to reflect the correct sum
                dictionary[lemm]['total'] += pop['total']

# Function to process a book and update word occurrences
def process_book(file_name):
    file_path = os.path.join(PRE_PATH, file_name)  # Use pre-path to create full file path
    book_name = file_name  # Use only the file name

    # Check if the book has already been processed
    if processed_books_collection.find_one({'book_name': book_name}):
        print(f"The book '{book_name}' has already been processed.")
        return  # Exit the function if the book has already been processed

    # Open and read the text file with utf-8 encoding
    with open(file_path, 'r', encoding='utf-8') as file:
        text = file.read()

    # Initialize a dictionary to store word occurrences
    word_occurrences = {}
    paragraphs = text.split("\n\n")  # Assume paragraphs are separated by two \n

    # Iterate through paragraphs
    for i, paragraph in enumerate(paragraphs):
        words = [clean_word(word) for word in paragraph.lower().split()]
        words = [word for word in words if word]  # Filter out None values after cleaning

        # Count how many times each word appears in the paragraph
        for word in set(words):  # Use set() to avoid duplicate counts
            word_count = words.count(word)

            if word not in word_occurrences:
                word_occurrences[word] = {
                    'count': 0,  # Initialize total count
                    'books': {}
                }

            # Increment the total count of the word
            word_occurrences[word]['count'] += word_count

            # Add book and paragraph information
            if book_name not in word_occurrences[word]['books']:
                word_occurrences[word]['books'][book_name] = {}

            # Add the paragraph number with the count of occurrences
            word_occurrences[word]['books'][book_name][f"paragraph {i}"] = word_count

    # Lemmatize words and save the occurrences in MongoDB
    for word in list(word_occurrences.keys()):
        lemm_add(word_occurrences, word)

        # Find the existing document
        existing_data = words_collection.find_one({'word': word})

        if existing_data:
            # If the word already exists, update the total count
            new_count = existing_data['count'] + word_occurrences[word]['count']

            # Update the list of books and paragraphs
            for book, paragraphs in word_occurrences[word]['books'].items():
                if book in existing_data['books']:
                    # Add only paragraphs not already present and update total count
                    for paragraph, count in paragraphs.items():
                        if paragraph not in existing_data['books'][book]:
                            existing_data['books'][book][paragraph] = count
                        else:
                            # If the paragraph already exists, sum the occurrence count
                            existing_data['books'][book][paragraph] += count
                else:
                    existing_data['books'][book] = paragraphs

            # Update the document in the database with the new count and books
            words_collection.update_one(
                {'word': word},
                {'$set': {'count': new_count, 'books': existing_data['books']}}
            )
            print(f"Updated {word}: {new_count} occurrences across books.")
        else:
            # If the word does not exist, insert it
            words_collection.insert_one({
                'word': word,
                'count': word_occurrences[word]['count'],  # Initialize count with the total count
                'books': word_occurrences[word]['books']
            })
            print(f"Inserted {word}: {word_occurrences[word]['count']} occurrences.")

    # Add the processed book to the collection
    processed_books_collection.insert_one({'book_name': book_name})
    print(f"The book '{book_name}' has been processed and recorded.")

# Main function to run the program
def execute(choice=None):
    if not choice:
        choice = input("Do you want to process a (1) single file or (2) all files in the folder? (1/2): ")

    if choice == '1':
        file_name = input("Enter the name of the file to process (without the path): ")
        process_book(file_name)
    elif choice == '2':
        folder_path = PRE_PATH  # Use pre-path as the folder path
        for filename in os.listdir(folder_path):
            if filename.endswith('.txt'):  # Ensure only .txt files are processed
                process_book(filename)
                print(f"Processed file: '{filename}'")
    else:
        print("Invalid choice. Please try again.")

    print("Data saved in the MongoDB database.")

# Execute the program
if __name__ == "__main__":
    execute()