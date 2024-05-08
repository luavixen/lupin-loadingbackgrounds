#!/bin/sh

cd ..

for loader in fabric forge neoforge; do

  echo "Creating links for loader $loader..."

  set -o xtrace

  cd $loader/src/main

  cd resources
  rm loadingbackgrounds.mixins.json
  ln -s ../../../../mixins/loadingbackgrounds.mixins.json loadingbackgrounds.mixins.json
  cd ..

  cd java/dev/foxgirl/loadingbackgrounds
  rm mixin
  ln -s ../../../../../../../mixins/mixin mixin
  cd ../../../..

  cd ../../..

  set +o xtrace

done
