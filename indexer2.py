import os
import spacy
from pymongo import MongoClient

# Load the language model
nlp = spacy.load("en_core_web_sm")

# Configure the MongoDB connection
client = MongoClient('mongodb://localhost:27017/')
db = client['SEARCH_ENGINE']  # Database name
words_collection = db['WORDS']  # Collection name for words
processed_books_collection = db['PROCESSED_BOOKS']  # Collection name for processed books

# Predefined pre-path
PRE_PATH = "datalake/books/"

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
        doc_paragraph = nlp(paragraph)

        # Extract common nouns (tokens of type NOUN)
        common_nouns = [token.text for token in doc_paragraph if token.pos_ == "NOUN"]

        # Count how many times each noun appears in the paragraph
        for noun in set(common_nouns):  # Use set() to avoid duplicate counts
            # Count occurrences of the word in the paragraph
            noun_count = common_nouns.count(noun)

            if noun not in word_occurrences:
                word_occurrences[noun] = {
                    'count': 0,  # Initialize total count
                    'books': {}
                }

            # Increment the total count of the word
            word_occurrences[noun]['count'] += noun_count

            # Add book and paragraph information
            if book_name not in word_occurrences[noun]['books']:
                word_occurrences[noun]['books'][book_name] = {}

            # Add the paragraph number with the count of occurrences
            word_occurrences[noun]['books'][book_name][f"paragraph {i}"] = noun_count

    # Save the occurrences in MongoDB
    for word, data in word_occurrences.items():
        # Find the existing document
        existing_data = words_collection.find_one({'word': word})

        if existing_data:
            # If the word already exists, update the total count
            new_count = existing_data['count'] + data['count']

            # Update the list of books and paragraphs
            for book, paragraphs in data['books'].items():
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
                'count': data['count'],  # Initialize count with the total count
                'books': data['books']
            })
            print(f"Inserted {word}: {data['count']} occurrences.")

    # Add the processed book to the collection
    processed_books_collection.insert_one({'book_name': book_name})
    print(f"The book '{book_name}' has been processed and recorded.")

# Main function to run the program
def execute():
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