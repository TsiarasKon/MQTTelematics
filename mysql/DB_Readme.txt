1. Go to \config.ini and change basedir, datadir and log-error paths. You may also optionally change the port number.
2. Start the server from cmd like:
        .\mysql-8.0.18-winx64\bin\mysqld.exe --defaults-file="..\..\config.ini"
3. Connect either via MySQL Workbench or via cmd like:
        .\mysql-8.0.18-winx64\bin\mysql.exe edge_server_db -u <username> -p --port <port>

Defaults:
username = root
password = root
port = 3306