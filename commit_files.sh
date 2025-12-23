#!/bin/bash

git status --porcelain | cut -c4- | while read -r file; do
  # Skip empty lines and deleted files
  if [ -z "$file" ]; then
    continue
  fi

  # Check if file exists (skip deleted files)
  if [ ! -e "$file" ]; then
    continue
  fi

  # Extract filename without path
  filename=$(basename "$file")

  # Generate commit message based on file extension
  case "$file" in
    *.java)
      msg="style: format $filename"
      ;;
    *.md)
      msg="docs: format $filename"
      ;;
    *.yml|*.yaml)
      msg="chore: format $filename"
      ;;
    *.json)
      msg="chore: format $filename"
      ;;
    *.gradle)
      msg="chore: format $filename"
      ;;
    *.groovy)
      msg="style: format $filename"
      ;;
    *.sql)
      msg="chore: format $filename"
      ;;
    *.properties)
      msg="chore: format $filename"
      ;;
    *)
      msg="chore: format $filename"
      ;;
  esac

  git add "$file" && git commit -m "$msg"
done

echo "All files committed!"
