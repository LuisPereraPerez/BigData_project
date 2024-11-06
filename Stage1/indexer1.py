import os
import json
import re
from unidecode import unidecode
from simplemma import lemmatize 

# Create necessary directories
OUTPUT_DIR = 'datamart/reverse_indexes/'
GLOBAL_INDEX_FILE = 'datamart/global_index.txt'

def create_directory(path):
    os.makedirs(path, exist_ok=True)

def read_lines(file_path):
    try:
        with open(file_path, 'r') as file:
            return file.readlines()
    except FileNotFoundError:
        print(f"The file '{file_path}' does not exist.")
        return []

def write_lines(file_path, lines):
    with open(file_path, 'w') as file:
        file.writelines(lines)

def read_json(file_path):
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as json_file:
            return json.load(json_file)
    return None

def write_json(file_path, data):
    with open(file_path, 'w', encoding='utf-8') as json_file:
        json.dump(data, json_file, ensure_ascii=False, indent=4)

def read_global_index():
    global_index_dict = {}
    if os.path.exists(GLOBAL_INDEX_FILE):
        lines = read_lines(GLOBAL_INDEX_FILE)
        for line in lines:
            line = line.strip()
            if line:
                parts = line.split(" : ")
                if len(parts) == 2:
                    word, books = parts
                    global_index_dict[word] = books.split(", ")
    return global_index_dict

def write_global_index(global_index_dict):
    lines = [f"{word} : {', '.join(books)}\n" for word, books in global_index_dict.items()]
    write_lines(GLOBAL_INDEX_FILE, lines)

def clean_word(word):
    word = re.sub(r'^_.*|_.*_|_.*$|[\d]+|[^\w\s]', '', word)
    return unidecode(word).strip('_')

def lemm_add(dictionary, word, lang='en'):
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

def index_book(book_id):
    dictionary = {}
    book_file_path = f"datalake/books/{book_id}.txt"

    with open(book_file_path, encoding='utf-8') as fp:
        for line_number, line in enumerate(fp, start=1):
            words = [clean_word(word) for word in line.lower().split()]
            for position, word in enumerate(words):
                if word:
                    if word not in dictionary:
                        dictionary[word] = {'allocations': {}, 'total': 0}
                    book_key = f"BookID_{book_id}"

                    if book_key not in dictionary[word]['allocations']:
                        dictionary[word]['allocations'][book_key] = {'times': 0, 'position': []}

                    # Create a tuple for the position
                    pos_tuple = (line_number, position + 1)
                    if pos_tuple not in dictionary[word]['allocations'][book_key]['position']:
                        dictionary[word]['allocations'][book_key]['position'].append(pos_tuple)
                        dictionary[word]['allocations'][book_key]['times'] += 1
                    
                    dictionary[word]['total'] += 1  # Increment total for each occurrence

    for word in list(dictionary.keys()):
        lemm_add(dictionary, word)

    save_index(dictionary, book_id)

def save_index(dictionary, book_id):
    reserved_words = ['con', 'prn', 'aux', 'nul', 'com1', 'com2', 'com3', 
                      'com4', 'com5', 'com6', 'com7', 'com8', 'com9', 
                      'lpt1', 'lpt2', 'lpt3', 'lpt4', 'lpt5', 'lpt6', 
                      'lpt7', 'lpt8', 'lpt9']

    for word, data in dictionary.items():
        first_letter = word[0].lower()
        if word.lower() in reserved_words:
            print(f"Word '{word}' skipped because it is a reserved name.")
            continue

        create_directory(f"datamart/reverse_indexes/{first_letter}")
        json_file_path = f"datamart/reverse_indexes/{first_letter}/{word}.json"

        existing_data = read_json(json_file_path) or {
            'word': word,
            'allocations': data['allocations'],
            'total': 0  # Initialize total to zero if no existing data
        }

        if existing_data:
            existing_allocations = existing_data['allocations']
            for book_key, new_info in data['allocations'].items():
                if book_key in existing_allocations:
                    # Merge positions without duplicates
                    existing_positions = set(tuple(pos) for pos in existing_allocations[book_key]['position'])  # Convert to set of tuples
                    existing_positions.update(tuple(pos) for pos in new_info['position'])  # Ensure new_info['position'] is also tuples
                    existing_allocations[book_key]['position'] = list(existing_positions)  # Convert back to list if needed
                    # Update count based on unique positions
                    existing_allocations[book_key]['times'] = len(existing_positions)
                else:
                    existing_allocations[book_key] = new_info
            
            # Update total to be the sum of all times in allocations
            existing_data['total'] = sum(info['times'] for info in existing_allocations.values())

        write_json(json_file_path, existing_data)

    update_global_index(dictionary, book_id)

def update_global_index(dictionary, book_id):
    global_index_dict = read_global_index()

    for word in dictionary.keys():
        if word in global_index_dict:
            if f"BookID_{book_id}" not in global_index_dict[word]:
                global_index_dict[word].append(f"BookID_{book_id}")
        else:
            global_index_dict[word] = [f"BookID_{book_id}"]

    write_global_index(global_index_dict)

def read_numbers_from_file(file_path):
    try:
        lines = read_lines(file_path)
        if len(lines) == 0:
            raise ValueError("The file is empty.")
        first_number = int(lines[0].strip())
        second_number = int(lines[1].strip()) if len(lines) > 1 else 1

        write_lines(file_path, [f"{first_number}\n", f"{first_number}\n"])
        return first_number, second_number
    except ValueError as e:
        print(f"Error reading numbers from file: {e}")
        return 0, 0
def update_last_book_id(file_path, new_first_number, new_second_number):
    write_lines(file_path, [f"{new_first_number}\n", f"{new_second_number}\n"])

        
def execute():
    n, m = read_numbers_from_file("resources/lastBookId.txt")
    for i in range(m, n + 1):
        index_book(i)
        print(f"Book with ID {i} has been indexed.")
    update_last_book_id("resources/lastBookId.txt", n, n + 1)

if __name__ == "__main__":
    execute()
