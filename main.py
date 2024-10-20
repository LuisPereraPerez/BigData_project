import crawler
import indexer1
import indexer2

def main():
    """Main function to execute the crawler and indexer modules."""
    print("Starting the book download process...")
    crawler.execute()
    
    print("Starting the indexing process 1...")
    indexer1.execute()
    
    print("Starting the indexing process 2...")
    indexer2.execute()

    print("Process completed.")

if __name__=="__main__":
    main()
