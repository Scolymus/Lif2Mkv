# Lif2Mkv

This software extracts all videos and photos from .lif files into tiff images (for Z-stack) and mp4 compressed videos with just pressing a button. In principle, there should not be a limitation in the size of the lif or the videos inside the lif file, although the RAM of your computer could play a role on it.

**HOW TO INSTALL IT IN ECLIPSE**

1) File->Import->Git->Projects from Git (with smart import)
2) Clone URI
3) Write "https://github.com/Scolymus/Lif2Mkv" in URI textfield (without quotes)
4) Next
5) Next
6) Next
7) Finish

**HOW TO RUN IT FROM ECLIPSE**

1) Run->Run as->Maven build...
2) Write "clean compile exec:java -Dexec.mainClass=main.main" (without quotes) in Goals textfield
3) Apply
4) Run

**HOW TO USE IT**

Drag and drop your lif files into the window.
By default, videos will be extracted to the folder where the .lif file is. You can change the output folder individually per video or per lif file modifying the field "Save in". You can also avoid extracting a video unclicking the check button in "Extract". 
Color bars indicate progress. Green is completed, altough sometimes there is a bug and it is still blue or yellow. 
You can change the compression rate at video quality (for videos bitrate) or Z-stack quality (for tiff compression).
In case your videos have more than one channel, you can change the name of the channels in "Channels" button. You can extract up to 10 channels. If you need less you do not need to change their names.
When your configuration is done, press "EXTRACT VIDEOS!" and wait.
