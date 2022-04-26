# PLBuildings plugin

## Description
PLBuildings is a [JOSM](https://josm.openstreetmap.de/) plugin 
which allows to easily import buildings from polish public datasets to add it to the OpenStreetMap.
It communicates with the [PLBuildings server](https://github.com/praszuk/josm-plbuildings-server) that obtains this data.

## How to use it
Move the cursor to a building and press `CTRL + SHIFT + 1`.
If you select other building before pressing shortcut combination, then it will use _Replace Geometry_ feature (from [utilsplugin2](https://wiki.openstreetmap.org/wiki/JOSM/Plugins/utilsplugin2))
to replace old shape and tags with the new one. It can show the conflict window.

## License
[GPLv3](LICENSE)