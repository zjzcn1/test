cmake_minimum_required(VERSION 3.2)

project(test)

############ set config ############
set(CMAKE_BUILD_TYPE "Release")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -O3")

link_directories("/usr/local/lib/")

############ find package ############
LIST(APPEND CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}/cmake_modules)

find_package(Boost 1.68.0 REQUIRED COMPONENTS system filesystem iostreams thread graph)
find_package(Eigen3 3.1.0 REQUIRED)

############ include ############
include_directories(
        include
        third_party
        ${CMAKE_CURRENT_BINARY_DIR}
        ${Boost_INCLUDE_DIRS}
        ${EIGEN3_INCLUDE_DIRS}
)

############ build src ############
file(GLOB_RECURSE SRCS "src/*.cpp")

add_library(${PROJECT_NAME} SHARED
        ${SRCS}
        )

############ link lib ############
target_link_libraries(${PROJECT_NAME}
        ${Boost_LIBRARIES}
        ${EIGEN3_LIBS}
        )
