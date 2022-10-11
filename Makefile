all:
	g++ -o app.bin \
	hog_cpp.cpp \
	-lopencv_core -lopencv_videoio -lopencv_highgui -lopencv_imgproc -lopencv_imgcodecs \
	-I /usr/include/opencv4