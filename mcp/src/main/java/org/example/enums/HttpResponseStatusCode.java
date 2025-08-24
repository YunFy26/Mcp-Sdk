package org.example.enums;

/**
 * HTTP 响应状态码常量
 */
public class HttpResponseStatusCode {

    // ==================================================
    // = 2xx
    // ==================================================
    /**
     * 200 OK - 请求成功，服务器已正常返回请求的资源
     */
    public static final int SUCCESS_200_OK = 200;

    /**
     * 201 Created - 请求成功且服务器已创建新的资源
     */
    public static final int SUCCESS_201_CREATED = 201;

    /**
     * 202 Accepted - 服务器已接受请求，但尚未处理完成（异步处理）
     */
    public static final int SUCCESS_202_ACCEPTED = 202;

    /**
     * 203 Non-Authoritative Information - 服务器返回的元数据由中间代理修改过
     */
    public static final int SUCCESS_203_NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * 204 No Content - 请求成功，但服务器没有返回任何内容
     */
    public static final int SUCCESS_204_NO_CONTENT = 204;

    /**
     * 205 Reset Content - 请求成功，服务器要求客户端重置视图
     */
    public static final int SUCCESS_205_RESET_CONTENT = 205;

    /**
     * 206 Partial Content - 服务器成功处理了部分GET请求（断点续传）
     */
    public static final int SUCCESS_206_PARTIAL_CONTENT = 206;

    // ==================================================
    // = 3xx
    // ==================================================
    /**
     * 300 Multiple Choices - 请求的资源有多个可能的返回值
     */
    public static final int REDIRECT_300_MULTIPLE_CHOICES = 300;

    /**
     * 301 Moved Permanently - 资源已永久移动到新位置
     */
    public static final int REDIRECT_301_MOVED_PERMANENTLY = 301;

    /**
     * 302 Found - 资源临时移动到新位置
     */
    public static final int REDIRECT_302_FOUND = 302;

    /**
     * 303 See Other - 应使用GET方法访问另一个URI获取资源
     */
    public static final int REDIRECT_303_SEE_OTHER = 303;

    /**
     * 304 Not Modified - 资源未修改，可使用缓存
     */
    public static final int REDIRECT_304_NOT_MODIFIED = 304;

    /**
     * 307 Temporary Redirect - 临时重定向，保持原请求方法
     */
    public static final int REDIRECT_307_TEMPORARY_REDIRECT = 307;

    /**
     * 308 Permanent Redirect - 永久重定向，保持原请求方法
     */
    public static final int REDIRECT_308_PERMANENT_REDIRECT = 308;

    // ==================================================
    // = 4xx
    // ==================================================
    /**
     * 400 Bad Request - 请求语法错误或参数无效
     */
    public static final int CLIENT_ERROR_400_BAD_REQUEST = 400;

    /**
     * 401 Unauthorized - 请求需要身份验证
     */
    public static final int CLIENT_ERROR_401_UNAUTHORIZED = 401;

    /**
     * 403 Forbidden - 服务器拒绝执行请求（权限不足）
     */
    public static final int CLIENT_ERROR_403_FORBIDDEN = 403;

    /**
     * 404 Not Found - 请求的资源不存在
     */
    public static final int CLIENT_ERROR_404_NOT_FOUND = 404;

    /**
     * 405 Method Not Allowed - 请求方法不被允许
     */
    public static final int CLIENT_ERROR_405_METHOD_NOT_ALLOWED = 405;

    /**
     * 406 Not Acceptable - 服务器无法提供请求头中指定的格式
     */
    public static final int CLIENT_ERROR_406_NOT_ACCEPTABLE = 406;

    /**
     * 408 Request Timeout - 服务器等待请求超时
     */
    public static final int CLIENT_ERROR_408_REQUEST_TIMEOUT = 408;

    /**
     * 409 Conflict - 请求与服务器当前状态冲突（如资源已存在）
     */
    public static final int CLIENT_ERROR_409_CONFLICT = 409;

    /**
     * 410 Gone - 请求的资源已永久删除
     */
    public static final int CLIENT_ERROR_410_GONE = 410;

    /**
     * 415 Unsupported Media Type - 请求体的媒体类型不被支持
     */
    public static final int CLIENT_ERROR_415_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * 429 Too Many Requests - 客户端请求过于频繁（限流）
     */
    public static final int CLIENT_ERROR_429_TOO_MANY_REQUESTS = 429;

}