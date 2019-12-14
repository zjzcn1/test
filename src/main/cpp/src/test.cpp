#include <iostream>

extern "C"
{
int say(char* text){
     std::cout<<text<<std::endl;
     return 10;
}
}