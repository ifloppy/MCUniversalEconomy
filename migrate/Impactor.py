#!/usr/bin/env python3
import os
from datetime import datetime
import re

def get_user_input():
    print("\nMCUniversalEconomy Migration Tool")
    print("--------------------------------")
    
    # Get table prefix
    table_prefix = input("Enter database table prefix (default 'mcue_'): ").strip()
    if not table_prefix:
        table_prefix = 'mcue_'
    if not table_prefix.endswith('_'):
        table_prefix += '_'
        
    # Get currency type
    currency_type = input("Enter currency type: ").strip()
    while not currency_type:
        print("Currency type is required!")
        currency_type = input("Enter currency type: ").strip()
        
    # Get input file
    input_file = input("Enter input SQL file path (default 'input.sql'): ").strip()
    if not input_file:
        input_file = 'input.sql'
        
    # Get output file
    output_file = input("Enter output SQL file path (default 'output.sql'): ").strip()
    if not output_file:
        output_file = 'output.sql'
        
    # Get server ID
    server_id = input("Enter server ID (optional): ").strip()
        
    return table_prefix, currency_type, input_file, output_file, server_id

def clean_uuid(hex_uuid):
    # Remove '0x' prefix and convert to standard UUID format
    hex_str = hex_uuid.replace('0x', '').lower()
    return f"{hex_str[:8]}-{hex_str[8:12]}-{hex_str[12:16]}-{hex_str[16:20]}-{hex_str[20:]}"

def process_sql(input_file, output_file, table_prefix='mcue_', currency_type='minecraft:max_universal_dollar', server_id=None):
    # Ensure input file exists
    if not os.path.exists(input_file):
        print(f"\nError: Input file {input_file} not found!")
        return False

    try:
        # Read input SQL file
        with open(input_file, 'r', encoding='utf-8') as sql_file:
            content = sql_file.read()
            
        # Extract data using regex
        pattern = r"\((0x[0-9A-F]+),'([^']+)',(\d+),([0-9.]+)\)"
        matches = re.finditer(pattern, content, re.IGNORECASE)
        
        # Write output SQL
        with open(output_file, 'w', encoding='utf-8') as sql_file:
            # Write SQL header
            sql_file.write(f"-- MCUniversalEconomy migration from Impactor Economy\n")
            sql_file.write(f"-- Generated at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            sql_file.write(f"-- Currency Type: {currency_type}\n\n")
            
            # Create accounts table if not exists
            sql_file.write(f"""CREATE TABLE IF NOT EXISTS {table_prefix}accounts (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    balance DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);\n\n""")

            # Create transactions table if not exists
            sql_file.write(f"""CREATE TABLE IF NOT EXISTS {table_prefix}transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_uuid VARCHAR(36),
    target_uuid VARCHAR(36),
    amount DECIMAL(20,2) NOT NULL,
    tax DECIMAL(20,2) NOT NULL,
    type VARCHAR(16) NOT NULL,
    server_id VARCHAR(36),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_uuid) REFERENCES {table_prefix}accounts(uuid),
    FOREIGN KEY (target_uuid) REFERENCES {table_prefix}accounts(uuid)
);\n\n""")

            # Process matches
            row_count = 0
            for match in matches:
                try:
                    hex_uuid = match.group(1)
                    curr = match.group(2)
                    balance = float(match.group(4))
                    
                    # Skip if currency doesn't match
                    if curr != currency_type:
                        continue
                        
                    # Convert UUID to standard format
                    uuid_str = clean_uuid(hex_uuid)
                    
                    # Generate INSERT statement for account with empty username
                    sql = f"""INSERT INTO {table_prefix}accounts (uuid, username, balance)
VALUES ('{uuid_str}', '', {balance:.2f})
ON DUPLICATE KEY UPDATE username = VALUES(username), balance = VALUES(balance);\n"""
                    
                    sql_file.write(sql)
                    
                    # Generate INSERT statement for transaction record
                    if balance > 0:
                        server_id_str = f"'{server_id}'" if server_id else "NULL"
                        sql = f"""INSERT INTO {table_prefix}transactions 
(source_uuid, target_uuid, amount, tax, type, server_id)
VALUES (NULL, '{uuid_str}', {balance:.2f}, 0.00, 'MIGRATION', {server_id_str});\n"""
                        sql_file.write(sql)
                    
                    row_count += 1
                    
                except Exception as e:
                    print(f"Warning: Error processing row: {match.group(0)}. Error: {str(e)}")
                    continue
                
            print(f"\nSuccessfully processed {row_count} records.")
            return True
            
    except Exception as e:
        print(f"\nError during processing: {str(e)}")
        return False

def main():
    print("\nWelcome to the Impactor Economy Migration Tool")
    print("============================================")
    
    while True:
        table_prefix, currency_type, input_file, output_file, server_id = get_user_input()
        
        print("\nMigration Settings:")
        print(f"- Table Prefix: {table_prefix}")
        print(f"- Currency Type: {currency_type}")
        print(f"- Input File: {input_file}")
        print(f"- Output File: {output_file}")
        print(f"- Server ID: {server_id if server_id else 'Not specified'}")
        
        confirm = input("\nProceed with migration? (y/n): ").strip().lower()
        if confirm != 'y':
            retry = input("Would you like to try again? (y/n): ").strip().lower()
            if retry != 'y':
                print("\nMigration cancelled.")
                return
            continue
            
        print("\nStarting migration process...")
        if process_sql(input_file, output_file, table_prefix, currency_type, server_id):
            print(f"\nMigration complete! Check {output_file} for the SQL statements.")
        
        break

if __name__ == "__main__":
    main()
