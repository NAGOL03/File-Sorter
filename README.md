---

# File Organizer

This project is a Windows only file organizer application designed to sort files based on user-defined configurations.

## Overview

The File Organizer allows users to define sorting configurations using a simple text-based format. Each configuration, known as a "Sorter", specifies criteria such as tags, target directories, source directories, file names, and extensions.

**File Sorter Folder Location:**

The File Sorter application creates a folder named "Sorters" in the user's home (`C:\Users\Username\Sorters`). This folder serves as the default location for storing sorter configuration files.

**Headless mode:**

The File Sorter can be run without a GUI. In your terminal, input the argument "headless" to run and sort all active sorters. EX: (Java FileSorter headless).

## Sorter Configuration Example

Here's an example of a Sorter configuration:

```
SORTER:
#Tag TEST;
#Target C:\Users\Example\User\Documents\SortedFiles;
#Active true;
@source C:\Users\Example\User\Downloads;
@name images;
@name documents;
@extension .jpg;
@extension .pdf;
```

In this configuration:
- The Sorter is tagged as "TEST".
- The target directory for sorted files is `C:\Users\Example\User\Documents\SortedFiles`.
- The Sorter is currently active.
- Files from the `C:\Users\Example\User\Downloads` directory are considered as sources.
- Files with names containing "images" or "documents" are selected.
- Only files with extensions ".jpg" or ".pdf" are included.

## GUI Interface

The application provides a graphical user interface (GUI) with the following features:

- **Run Button**: Initiates the sorting process based on the defined configurations.
- **Refresh Button**: Updates the list of Sorters displayed in the interface.
- **Sorter Indicator**: Displays an indicator next to each Sorter to show whether it is currently active or not.

## Project Status

This project is currently incomplete, and some code redundancy exists. Development has been paused, and the project may be revisited in the future for further improvements.

---
