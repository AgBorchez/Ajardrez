#pragma once
#include <fstream>
#include <string>
#include <chrono>
#include <iomanip>

class Logger {
public:
    static void log(const std::string& tag, const std::string& message) {
        static std::ofstream log_file("engine.log", std::ios::app);
        if (!log_file.is_open()) return;

        auto now = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
        log_file << "[" << std::put_time(std::localtime(&now), "%Y-%m-%d %H:%M:%S") << "] "
                 << "[" << tag << "] " << message << "\n";
        log_file.flush();
    }
};