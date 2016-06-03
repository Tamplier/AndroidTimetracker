# Android Timetracker
Simple example of how to trace the applications started on the device.

Basic elements:

1. Activity to show results (list with last 10 applications icons, names and active time).

2. Service with separate thread to monitor active application.

3. SQLite db helper to store the data.

4. DBQueryWrapper for thread-safe interaction with db and for wrapping queries.

5. Messengers for interaction between Activity and Service.