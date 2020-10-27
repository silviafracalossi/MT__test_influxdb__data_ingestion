awk '{ sub("\r$", ""); print }' code.sh > fake.sh
mv fake.sh code.sh
bash code.sh $1
