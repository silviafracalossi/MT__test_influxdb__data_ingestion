awk '{ sub("\r$", ""); print }' code_index.sh > fake.sh
mv fake.sh code_index.sh
bash code_index.sh
