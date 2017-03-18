# Changelog

# 1.2.0

- Allow scraping of universities which provide multiple grade entry tables for each semester
- Improve redirect handling
- Add new weekly scraping interval
- Add setting to hide credit points
- Fix: Update baseUri after location redirect

# 1.1.2

- Added calculation of participants and average for overview if it is not given

# 1.1.1

- Minor fix to remove invalid html

# 1.1.0

- Added extensive edit mode to modify grade entries
 - change all properties (grade, cp, weight, ...)
 - move grade entry to another semester
 - hide unwanted grade entries
 - add and delete custom grade entries
- Appended *attempt* to grade entry hash, because some universities may not provide enough distinct information for each entry
- Added property *weight* to GradeEntry to allow better average calculation
- Fix: Alarm settings are now recreated after an app upgrade to receive notifications for new grades
- Updated FAQs

# 1.0.9

- Fix: AverageCalculator now adds credit points without grade to overall sum of credit points. Last fix ignored *null* values for grades.

# 1.0.8

- Fix: AverageCalculator adds credit points without grade to overall sum of credit points