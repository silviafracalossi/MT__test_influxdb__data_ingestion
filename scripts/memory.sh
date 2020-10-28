awk '{ sub("\r$", ""); print }' code_memory.sh > fake.sh
mv fake.sh code_memory.sh
bash code_memory.sh
