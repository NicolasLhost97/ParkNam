# ParkNam

Pour pouvoir tester l'application, il vous faut la partie Back-end: https://github.com/AymVel/ParkNam-Back.git

Une fois les deux parties téléchargées, vous devez dans un premier temps lancer le back en suivant les instructions dans le readme.

Pour lier l'application au back, il vous faut remplacer l'adresse IP du back à deux endroit dans le code. Ligne 115 dans la variable baseURL et à la ligne 154 dans la variable FREE_PLACES_SOURCE_URL. Faites bien attention à garder le "http://" au début et le ":800/" à la fin. 
Pour trouver l'adresse du back, ouvrez un terminal de commande et tapez "ipconfig" sur Windows et "ifconfig" sur MacOS et cherchez l'adresse publique (elle commence souvent par 192.xxx)

Une fois cette adresse changée aux 2 endroits dans le code, il suffit de run l'application sur un émulateur ou un téléphone physique. Pour ce faire, privilégiez Android studio
