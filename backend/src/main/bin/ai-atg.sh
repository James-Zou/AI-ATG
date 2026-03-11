#!/bin/bash

# ============================================
# AI-ATG 服务管理脚本
# 用法: ./ai-atg.sh {start|stop|restart|status}
# ============================================

# 获取脚本所在目录
cd "$(dirname "$0")" || exit 1
BIN_DIR=$(pwd)
cd ..
APP_HOME=$(pwd)

# 配置变量
APP_NAME="ai-atg-backend"
JAR_NAME="ai-atg-backend-1.0.0.jar"
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/app.pid"

# JVM配置
JVM_OPTS="-server"
JVM_OPTS="${JVM_OPTS} -Xms512m -Xmx2g"
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"
JVM_OPTS="${JVM_OPTS} -XX:MaxGCPauseMillis=200"
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof"
JVM_OPTS="${JVM_OPTS} -Dfile.encoding=UTF-8"

# Spring Boot配置
SPRING_OPTS="--spring.config.location=${CONFIG_DIR}/application.yml"
SPRING_OPTS="${SPRING_OPTS} --logging.file.path=${LOG_DIR}"

# ============================================
# 函数定义
# ============================================

# 启动函数
start() {
    # 创建日志目录
    mkdir -p "${LOG_DIR}"
    
    # 检查是否已经启动
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p "${PID}" > /dev/null 2>&1; then
            echo "应用已经在运行中 (PID: ${PID})"
            return 1
        else
            echo "删除过期的PID文件"
            rm -f "${PID_FILE}"
        fi
    fi
    
    # 启动应用
    echo "=========================================="
    echo "启动 ${APP_NAME}"
    echo "=========================================="
    echo "APP_HOME: ${APP_HOME}"
    echo "CONFIG_DIR: ${CONFIG_DIR}"
    echo "LOG_DIR: ${LOG_DIR}"
    echo "=========================================="
    
    nohup /data/app/jdk/jre/bin/java ${JVM_OPTS} -jar "${LIB_DIR}/${JAR_NAME}" ${SPRING_OPTS} > "${LOG_DIR}/console.log" 2>&1 &
    
    # 保存进程ID
    echo $! > "${PID_FILE}"
    PID=$(cat "${PID_FILE}")
    
    echo "应用正在启动中..."
    sleep 3
    
    # 检查是否启动成功
    if ps -p "${PID}" > /dev/null 2>&1; then
        echo "=========================================="
        echo "应用启动成功 (PID: ${PID})"
        echo "日志文件: ${LOG_DIR}/console.log"
        echo "=========================================="
        return 0
    else
        echo "应用启动失败，请查看日志: ${LOG_DIR}/console.log"
        rm -f "${PID_FILE}"
        return 1
    fi
}

# 停止函数
stop() {
    echo "=========================================="
    echo "停止 ${APP_NAME}"
    echo "=========================================="
    
    # 检查PID文件是否存在
    if [ ! -f "${PID_FILE}" ]; then
        echo "应用未运行或PID文件不存在"
        return 1
    fi
    
    # 读取PID
    PID=$(cat "${PID_FILE}")
    
    # 检查进程是否存在
    if ! ps -p "${PID}" > /dev/null 2>&1; then
        echo "进程不存在 (PID: ${PID})"
        rm -f "${PID_FILE}"
        return 1
    fi
    
    # 停止应用
    echo "正在停止应用 (PID: ${PID})..."
    kill "${PID}"
    
    # 等待进程结束
    WAIT_TIME=0
    MAX_WAIT=30
    while ps -p "${PID}" > /dev/null 2>&1; do
        sleep 1
        WAIT_TIME=$((WAIT_TIME + 1))
        
        if [ ${WAIT_TIME} -eq ${MAX_WAIT} ]; then
            echo "应用未在${MAX_WAIT}秒内停止，强制终止..."
            kill -9 "${PID}"
            sleep 2
            break
        fi
    done
    
    # 删除PID文件
    rm -f "${PID_FILE}"
    
    if ps -p "${PID}" > /dev/null 2>&1; then
        echo "应用停止失败"
        return 1
    else
        echo "=========================================="
        echo "应用已停止"
        echo "=========================================="
        return 0
    fi
}

# 重启函数
restart() {
    echo "=========================================="
    echo "重启 ${APP_NAME}"
    echo "=========================================="
    
    # 停止应用
    stop
    
    # 等待2秒
    sleep 2
    
    # 启动应用
    start
}

# 状态检查函数
status() {
    echo "=========================================="
    echo "${APP_NAME} 状态检查"
    echo "=========================================="
    
    # 检查PID文件
    if [ ! -f "${PID_FILE}" ]; then
        echo "状态: 未运行 (PID文件不存在)"
        return 1
    fi
    
    # 读取PID
    PID=$(cat "${PID_FILE}")
    
    # 检查进程
    if ps -p "${PID}" > /dev/null 2>&1; then
        echo "状态: 运行中"
        echo "PID: ${PID}"
        echo "=========================================="
        ps -p "${PID}" -o pid,ppid,user,%cpu,%mem,etime,cmd
        echo "=========================================="
        return 0
    else
        echo "状态: 未运行 (进程不存在)"
        echo "PID文件存在但进程已停止，建议删除: ${PID_FILE}"
        return 1
    fi
}

# 显示使用说明
usage() {
    echo "=========================================="
    echo "AI-ATG 服务管理脚本"
    echo "=========================================="
    echo "用法: $0 {start|stop|restart|status}"
    echo ""
    echo "命令说明:"
    echo "  start   - 启动应用"
    echo "  stop    - 停止应用"
    echo "  restart - 重启应用"
    echo "  status  - 查看应用状态"
    echo "=========================================="
}

# ============================================
# 主程序入口
# ============================================

# 检查参数
if [ $# -ne 1 ]; then
    usage
    exit 1
fi

# 根据参数执行对应操作
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        echo "错误: 未知命令 '$1'"
        echo ""
        usage
        exit 1
        ;;
esac

exit $?
