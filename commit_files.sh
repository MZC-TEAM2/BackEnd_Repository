#!/bin/bash

# Get list of modified files
git status --porcelain | while read -r line; do
  file="${line:3}"

  if [ -z "$file" ]; then
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

  # Add and commit
  git add "$file" && git commit -m "$msg"
done

echo "All files committed!"
