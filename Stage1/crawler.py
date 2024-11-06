# Part to download books from the Gutenberg website

import requests
import os
import csv
from bs4 import BeautifulSoup
import re

# Create directories to store books and metadata if they do not exist
os.makedirs('datalake/books', exist_ok=True)
os.makedirs('datalake', exist_ok=True)

BOOKS_TO_DOWNLOAD = 5
METADATA_CSV_FILE = 'datalake/metadata.csv'

def execute():
    """Main function to download eBooks and their metadata."""
    id_limit_book_downloaded = check_limit_id_books()
    id_book = id_limit_book_downloaded + 1
    count_downloaded_books = 0

    # Create the CSV file and write the header if it doesn't exist
    if not os.path.exists(METADATA_CSV_FILE):
        with open(METADATA_CSV_FILE, 'w', newline='', encoding='utf-8') as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(["ID", "Title", "Author", "Release Date", "Most Recently Updated", "Language"])

    while count_downloaded_books < BOOKS_TO_DOWNLOAD:
        if not check_book_exist(id_book):
            title = download_ebook(id_book)
            if title:  # Proceed only if the download was successful
                metadata = obtain_metadata(id_book, title)
                if metadata:  # Only process if metadata was retrieved successfully
                    append_metadata_to_csv(metadata)
                processing_book(id_book, title)
                os.remove(f"datalake/books/{id_book} {title}.txt")
                count_downloaded_books += 1
        id_book += 1

    # Save the ID and overwrite only the first line of the lastBookId.txt file
    with open("resources/lastBookId.txt", "r+") as file:
        lines = file.readlines()  # Read all lines from the file
        if lines:
            lines[0] = str(id_book - 1) + "\n"  # Update only the first line
        else:
            lines.append(str(id_book - 1) + "\n")  # If empty, add the first line

        # Go back to the start of the file and overwrite lines
        file.seek(0)
        file.writelines(lines)
        file.truncate()  # Remove any leftover content if lines were shortened

def download_ebook(id_book):
    """Download the eBook by its ID and return its title."""
    try:
        book_url = f"https://www.gutenberg.org/ebooks/{id_book}"
        response = requests.get(book_url)  # Make a request to the book's URL (ID)
        if response.status_code == 200:
            soup = BeautifulSoup(response.text, 'html.parser')
            title_tag = soup.find('h1')  # Find the h1 tag for the title
            title = title_tag.get_text() if title_tag else f"Unknown Title id {id_book}"
            text_link = soup.find('a', href=True, string="Plain Text UTF-8")  # Find the link to the plain text file
            if text_link:
                link_txt = 'https://www.gutenberg.org' + text_link['href']
                response_txt = requests.get(link_txt)
                if response_txt.status_code == 200:
                    title = re.sub(r'[^\w\s]', '', title)  # Clean title
                    with open(f"datalake/books/{id_book} {title}.txt", 'w', encoding='utf-8') as file:
                        file.write(response_txt.text)  # Write the book content to the text file
                        print(f"Book with ID {id_book} has been downloaed.")
                    return title
                else:
                    print(f"Error accessing the text file of the book with ID {id_book}: {response_txt.status_code}")
            else:
                print(f"The book with ID {id_book} does not have a text file available.")
        else:
            print(f"Error accessing the book with ID {id_book}: {response.status_code}")
    except Exception as e:
        print(f"Error accessing the book with ID {id_book}: {e}")

def obtain_metadata(id_book, title):
    """Obtain metadata from the downloaded eBook."""
    try:
        with open(f"datalake/books/{id_book} {title}.txt", 'r', encoding='utf-8') as file:
            text = file.read()
            metadata = {
                "ID": id_book,
                "Title": re.search(r"Title: (.+)", text).group(1) if re.search(r"Title: (.+)", text) else "Unknown Title",
                "Author": re.search(r"Author: (.+)", text).group(1) if re.search(r"Author: (.+)", text) else "Unknown Author",
                "Release Date": re.search(r"Release Date: (.+)", text).group(1) if re.search(r"Release Date: (.+)", text) else "Unknown",
                "Most Recently Updated": re.search(r"Most recently updated: (.+)", text).group(1) if re.search(r"Most recently updated: (.+)", text) else "Unknown",
                "Language": re.search(r"Language: (.+)", text).group(1) if re.search(r"Language: (.+)", text) else "Unknown",
            }
            return metadata  # Return the metadata for further processing
    except Exception as e:
        print(f"Error accessing the book with ID {id_book}: {e}")
        return None  # Return None if an error occurred

def append_metadata_to_csv(metadata):
    """Append the metadata of the book to the CSV file."""
    with open(METADATA_CSV_FILE, 'a', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow([metadata["ID"], metadata["Title"], metadata["Author"], metadata["Release Date"],
                         metadata["Most Recently Updated"], metadata["Language"]])

def processing_book(id_book, title):
    """Process the downloaded eBook to extract the main content."""
    try:
        with open(f"datalake/books/{id_book} {title}.txt", 'r', encoding='utf-8') as file:
            text = file.read()
            content_start = re.search(r"\*\*\* START OF THE PROJECT GUTENBERG EBOOK .+ \*\*\*", text)
            content_end = re.search(r"\*\*\* END OF THE PROJECT GUTENBERG EBOOK .+ \*\*\*", text)

            if content_start and content_end:
                raw_content = text[content_start.end():content_end.start()].strip()
                lines = raw_content.splitlines()
                
                # Process the lines into paragraphs
                paragraphs = []
                current_paragraph = []
                empty_line_count = 0
                
                for line in lines:
                    stripped_line = line.rstrip()
                    if stripped_line:
                        if empty_line_count > 2:
                            paragraphs.append("")  # Add a line break for paragraph separation
                        current_paragraph.append(stripped_line)
                        empty_line_count = 0
                    else:
                        if current_paragraph:
                            paragraphs.append(" ".join(current_paragraph))
                            current_paragraph = []
                        empty_line_count += 1

                if current_paragraph:
                    paragraphs.append(" ".join(current_paragraph))

                final_content = "\n".join(paragraphs)

                # Save processed content to a new file
                with open(f"datalake/books/{id_book}.txt", 'w', encoding='utf-8') as file:
                    file.write(final_content)

    except Exception as e:
        print(f"Error accessing the book with ID {id_book}: {e}")

def check_book_exist(id_book):
    """Check if the book with the given ID already exists in the download directory."""
    for filename in os.listdir('datalake/books/'):
        if filename.startswith(f"{id_book}"):
            print(f"The book with ID {id_book} has already been downloaded.")
            return True
    return False

def check_limit_id_books():
    """Check the last downloaded book ID from the lastBookId.txt file."""
    if os.path.exists("resources/lastBookId.txt"):
        with open("resources/lastBookId.txt", "r") as file:
            first_line = file.readline().strip()  # Read the first line and strip whitespace
            return int(first_line) if first_line.isdigit() else 0  # Check if it's a number
    else:
        with open("resources/lastBookId.txt", "w") as file:
            file.write("0\n")
        return 0

if __name__ == "__main__":
    execute()
