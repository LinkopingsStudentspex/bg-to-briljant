#!/bin/bash
cd $(git rev-parse --show-toplevel)
lein uberjar
version=$(head -n 1 project.clj | cut -d " " -f 3 | sed s/\"//g)
tar -czf bg-to-briljant-${version}.tar.gz target/uberjar/bg-to-briljant-${version}-standalone.jar Manual.md settings.edn
