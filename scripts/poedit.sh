#!/bin/bash

PO_LANG="pl"

generate() {
  ./gradlew generatePot
  msgmerge -U src/main/po/${PO_LANG}.po build/i18n/pot/josm-plugin_plbuildings.pot
  rm -f src/main/po/${PO_LANG}.po~
}

main() {
  generate
  poedit src/main/po/${PO_LANG}.po
  generate
}

main
