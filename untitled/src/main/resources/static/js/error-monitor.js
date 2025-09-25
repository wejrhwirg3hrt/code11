// 前端错误监控系统
class ErrorMonitor {
    constructor() {
        this.errors = [];
        this.maxErrors = 50; // 最多保存50个错误
        this.init();
    }

    init() {
        // 监听JavaScript错误
        window.addEventListener('error', (event) => {
            this.logError('JavaScript Error', {
                message: event.message,
                filename: event.filename,
                lineno: event.lineno,
                colno: event.colno,
                error: event.error ? event.error.stack : null
            });
        });

        // 监听Promise未处理的拒绝
        window.addEventListener('unhandledrejection', (event) => {
            this.logError('Unhandled Promise Rejection', {
                reason: event.reason,
                promise: event.promise
            });
        });

        // 监听网络错误
        this.monitorNetworkErrors();

        // 监听控制台错误
        this.monitorConsoleErrors();
        window.addEventListener('error', (event) => {
            this.logError({
                type: 'JavaScript Error',
                message: event.message,
                filename: event.filename,
                lineno: event.lineno,
                colno: event.colno,
                stack: event.error ? event.error.stack : null,
                timestamp: new Date().toISOString(),
                url: window.location.href,
                userAgent: navigator.userAgent
            });
        });

        // 监听Promise拒绝
        window.addEventListener('unhandledrejection', (event) => {
            this.logError({
                type: 'Unhandled Promise Rejection',
                message: event.reason ? event.reason.toString() : 'Unknown promise rejection',
                stack: event.reason && event.reason.stack ? event.reason.stack : null,
                timestamp: new Date().toISOString(),
                url: window.location.href,
                userAgent: navigator.userAgent
            });
        });

        // 监听资源加载错误
        window.addEventListener('error', (event) => {
            if (event.target !== window) {
                this.logError({
                    type: 'Resource Load Error',
                    message: `Failed to load resource: ${event.target.src || event.target.href}`,
                    element: event.target.tagName,
                    src: event.target.src || event.target.href,
                    timestamp: new Date().toISOString(),
                    url: window.location.href,
                    userAgent: navigator.userAgent
                });
            }
        }, true);

        // 监听网络错误
        this.monitorNetworkErrors();

        // 监听页面性能
        this.monitorPagePerformance();

        // 定期发送错误报告
        setInterval(() => this.sendErrorReport(), 30000); // 每30秒发送一次
    }

    logError(errorInfo) {
        console.error('Error detected:', errorInfo);
        
        this.errors.push(errorInfo);
        
        // 保持错误数量在限制内
        if (this.errors.length > this.maxErrors) {
            this.errors.shift();
        }

        // 立即发送严重错误
        if (this.isCriticalError(errorInfo)) {
            this.sendErrorToServer(errorInfo);
        }

        // 显示错误通知（仅在开发模式）
        if (this.isDevelopmentMode()) {
            this.showErrorNotification(errorInfo);
        }
    }

    isCriticalError(errorInfo) {
        const criticalKeywords = [
            'network error',
            'failed to fetch',
            'connection refused',
            'timeout',
            'server error',
            'authentication',
            'authorization'
        ];
        
        const message = errorInfo.message.toLowerCase();
        return criticalKeywords.some(keyword => message.includes(keyword));
    }

    isDevelopmentMode() {
        return window.location.hostname === 'localhost' || 
               window.location.hostname === '127.0.0.1' ||
               window.location.search.includes('debug=true');
    }

    showErrorNotification(errorInfo) {
        // 创建错误通知
        const notification = document.createElement('div');
        notification.className = 'error-notification';
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #dc3545;
            color: white;
            padding: 15px;
            border-radius: 5px;
            max-width: 400px;
            z-index: 10000;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        `;
        
        notification.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 5px;">${errorInfo.type}</div>
            <div style="font-size: 0.9em;">${errorInfo.message}</div>
            <button onclick="this.parentElement.remove()" style="
                position: absolute;
                top: 5px;
                right: 10px;
                background: none;
                border: none;
                color: white;
                font-size: 18px;
                cursor: pointer;
            ">&times;</button>
        `;
        
        document.body.appendChild(notification);
        
        // 自动移除通知
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }

    monitorConsoleErrors() {
        // 重写console.error以监控控制台错误
        const originalConsoleError = console.error;
        console.error = (...args) => {
            this.logError({
                type: 'Console Error',
                message: args.join(' '),
                timestamp: new Date().toISOString()
            });
            originalConsoleError.apply(console, args);
        };

        // 重写console.warn以监控警告
        const originalConsoleWarn = console.warn;
        console.warn = (...args) => {
            this.logError({
                type: 'Console Warning',
                message: args.join(' '),
                timestamp: new Date().toISOString()
            });
            originalConsoleWarn.apply(console, args);
        };
    }

    monitorNetworkErrors() {
        // 重写fetch以监控网络请求
        const originalFetch = window.fetch;
        window.fetch = async (...args) => {
            try {
                const response = await originalFetch(...args);
                
                if (!response.ok) {
                    this.logError({
                        type: 'Network Error',
                        message: `HTTP ${response.status}: ${response.statusText}`,
                        url: args[0],
                        status: response.status,
                        timestamp: new Date().toISOString(),
                        userAgent: navigator.userAgent
                    });
                }
                
                return response;
            } catch (error) {
                this.logError({
                    type: 'Network Error',
                    message: error.message,
                    url: args[0],
                    stack: error.stack,
                    timestamp: new Date().toISOString(),
                    userAgent: navigator.userAgent
                });
                throw error;
            }
        };
    }

    monitorPagePerformance() {
        window.addEventListener('load', () => {
            setTimeout(() => {
                const perfData = performance.getEntriesByType('navigation')[0];
                if (perfData) {
                    const loadTime = perfData.loadEventEnd - perfData.loadEventStart;
                    
                    // 记录页面访问
                    this.logPageVisit({
                        page: window.location.pathname,
                        loadTime: loadTime,
                        timestamp: new Date().toISOString()
                    });
                    
                    // 如果加载时间过长，记录为性能问题
                    if (loadTime > 5000) { // 5秒
                        this.logError({
                            type: 'Performance Issue',
                            message: `Page load time: ${loadTime}ms`,
                            loadTime: loadTime,
                            timestamp: new Date().toISOString(),
                            url: window.location.href,
                            userAgent: navigator.userAgent
                        });
                    }
                }
            }, 1000);
        });
    }

    logPageVisit(visitData) {
        fetch('/api/logs/page-visit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(visitData)
        }).catch(error => {
            console.error('Failed to log page visit:', error);
        });
    }

    sendErrorToServer(errorInfo) {
        fetch('/api/logs/frontend-error', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(errorInfo)
        }).catch(error => {
            console.error('Failed to send error to server:', error);
        });
    }

    sendErrorReport() {
        if (this.errors.length === 0) return;
        
        const report = {
            errors: [...this.errors],
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent
        };
        
        // 发送错误报告
        this.sendErrorToServer({
            type: 'Error Report',
            message: `Batch error report: ${this.errors.length} errors`,
            details: JSON.stringify(report),
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent
        });
        
        // 清空已发送的错误
        this.errors = [];
    }

    // 手动记录错误的方法
    reportError(message, details = {}) {
        this.logError({
            type: 'Manual Error Report',
            message: message,
            details: JSON.stringify(details),
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent
        });
    }

    // 获取错误统计
    getErrorStats() {
        const stats = {};
        this.errors.forEach(error => {
            stats[error.type] = (stats[error.type] || 0) + 1;
        });
        return stats;
    }
}

// 初始化错误监控
const errorMonitor = new ErrorMonitor();

// 暴露到全局作用域以便手动使用
window.errorMonitor = errorMonitor;
