#!/bin/sh
echo "Deploying"

proj=$1

BASE=/opt/dashboard
INST=/opt/dashboard/active

echo "Unpacking to $BASE"
tar xjf $proj.tar.bz2 -C $BASE || (echo Failed to untar; exit $1)

echo "Stopping services"
cd $INST
make stop

echo "Relinking $INST and copying old directories"
cp $INST/sites ~/
[ -h $INST ] && rm $INST && ln -s $BASE/$proj $INST
mv -f ~/sites $INST/

echo "Starting services"
cd $INST
make start

echo "Deployed"
exit 0