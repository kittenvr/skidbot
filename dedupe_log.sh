#!/bin/bash

# Simple script to remove duplicates from log.txt
# Usage: ./dedupe_log.sh [input_file] [output_file]

INPUT_FILE="${1:-/home/pizzav/.config/JetBrains/IdeaIC2025.2/scratches/scratch.txt}"
OUTPUT_FILE="${2:-log.txt}"

if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Error: Input file '$INPUT_FILE' not found"
    exit 1
fi

echo "Processing $INPUT_FILE -> $OUTPUT_FILE"
echo "Removing line numbers and duplicate entries..."

# Remove line numbers (format: 00001| ), deduplicate, and preserve order
awk '
{
    # Remove line numbers in format "00001| " and leading/trailing whitespace
    gsub(/^[0-9]+\| /, "")
    gsub(/^ +| +$/, "")
    
    # Skip empty lines
    if (length($0) == 0) next
    
    # Store unique lines while preserving order
    if (!seen[$0]++) {
        print $0
    }
}
' "$INPUT_FILE" > "$OUTPUT_FILE"

echo "Deduplication complete!"
echo "Original lines: $(wc -l < "$INPUT_FILE")"
echo "Deduplicated lines: $(wc -l < "$OUTPUT_FILE")"
rm "$INPUT_FILE"