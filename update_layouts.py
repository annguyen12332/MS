#!/usr/bin/env python3
"""
Script to update all HTML templates to use modern-layout
"""
import os
import re

# Base directory
templates_dir = r"D:\Tuan\project\MS\MS\src\main\resources\templates"

# Files to update (excluding login and register as they don't use layout)
files_to_update = [
    "admin/certificates/eligible.html",
    "admin/certificates/list.html",
    "admin/certificates/view.html",
    "admin/classes/form.html",
    "admin/classes/list.html",
    "admin/classes/view.html",
    "admin/courses/form.html",
    "admin/courses/list.html",
    "admin/courses/view.html",
    "admin/enrollments/list-by-class.html",
    "admin/enrollments/list.html",
    "admin/enrollments/pending.html",
    "admin/enrollments/view.html",
    "admin/schedules/form.html",
    "admin/schedules/list.html",
    "admin/users/form.html",
    "admin/users/list.html",
    "admin/users/view.html",
    "error/403.html",
    "error/404.html",
    "error/500.html",
    "profile/view.html",
]

def update_file(filepath):
    """Update a single file to use modern-layout"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Replace material-layout with modern-layout
        updated = re.sub(
            r'layout:decorate="~\{layout/material-layout\}"',
            'layout:decorate="~{layout/modern-layout}"',
            content
        )

        # Replace main-layout with modern-layout
        updated = re.sub(
            r'layout:decorate="~\{layout/main-layout\}"',
            'layout:decorate="~{layout/modern-layout}"',
            updated
        )

        # Change lang to vi if it's en
        updated = re.sub(
            r'<html lang="en"',
            '<html lang="vi"',
            updated
        )

        # Only write if changed
        if updated != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(updated)
            print(f"[OK] Updated: {filepath}")
            return True
        else:
            print(f"[SKIP] No change: {filepath}")
            return False

    except Exception as e:
        print(f"[ERROR] Error updating {filepath}: {e}")
        return False

def main():
    """Main function"""
    print("=" * 60)
    print("Updating HTML templates to use modern-layout")
    print("=" * 60)

    updated_count = 0
    for file_path in files_to_update:
        full_path = os.path.join(templates_dir, file_path)
        if os.path.exists(full_path):
            if update_file(full_path):
                updated_count += 1
        else:
            print(f"! File not found: {full_path}")

    print("=" * 60)
    print(f"Summary: {updated_count} files updated")
    print("=" * 60)

if __name__ == "__main__":
    main()
