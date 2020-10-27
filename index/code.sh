tsi="tsi1"
inmem="inmem"
if [ "$1" == "$tsi" ]; then
    echo "From inmem to tsi1!"
    cd /var/lib/influxdb/data
    sudo rm -r _internal/_series
    sudo rm -r test_table
    cd /etc/influxdb
    sudo sed -i 's/index-version="inmem"/index-version="tsi1"/' influxdb.conf
    echo "Done!"
else
    if [ "$1" == "$inmem" ]; then
        echo "From tsi1 to inmem!"
        cd /var/lib/influxdb/data
        sudo rm -r _internal/_series
        sudo rm -r test_table
        cd /etc/influxdb
        sudo sed -i 's/index-version="tsi1"/index-version="inmem"/' influxdb.conf
        echo "Done!"
    else
        echo "Wrong selection. Input \"tsi1\" or \"inmem\""
    fi
fi
