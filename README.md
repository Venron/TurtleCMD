<b>Vorwort.</b> Hier finden Sie die zugehörige Serversoftware für den Turtle Client. Da die Softwareentwicklung in meinem aktuellen 
Studium etwas weniger behandelt wird, als ich es mir eigentlich wünschen würde, habe ich mir viel selbst beibringen müssen, 
somit bitte ich um Nachsicht, wenn nicht immer eine klare Struktur erkennbar ist und ebenfalls nicht immer die erwartete Funktionalität 
geboten ist.

<b>Funktionsweise:</b>
  - Der Client benötigt folgende Programmparameter: > client.jar <host> <port>
  - Der Authentifizierungsserver benötigt keine Programmparameter. Er startet standardmäßig auf Port 55030. Damit der Auth.server
  Benutzer identifizieren kann, benötigt er die Datei "users.txt" im selben Ordner, wie die Jar-Datei. Neue Benutzer werden
  zeilenweise im Format "user,password" eingetragen
  - Der Chat Server startet standardmäßig auf Port 55021.
  
<b>Anmerkung.</b> Die Distribution (Broadcast) der gesendeten Nachrichten funktioniert in der aktuellen Version noch nicht. Der
Fehler ist jedoch identifiziert und wird in der kommenden Version behoben.
