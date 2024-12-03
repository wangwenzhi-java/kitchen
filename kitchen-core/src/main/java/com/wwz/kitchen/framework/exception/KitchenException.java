package com.wwz.kitchen.framework.exception;

/**
 *
 * 自定义全局异常处理类
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class KitchenException extends RuntimeException{

    /**
     * 默认构造函数，调用父类的无参构造方法。
     * 当没有传入异常信息时，使用此构造函数。
     */
    public KitchenException() {
        super();
    }
    /**
     * 带有错误信息的构造函数，调用父类的构造方法来初始化异常信息。
     *
     * @param message 异常信息，用于描述发生的错误。
     */
    public KitchenException(String message) {
        super(message);
    }
    /**
     * 带有错误信息和根本原因的构造函数，调用父类的构造方法来初始化异常信息和异常原因。
     *
     * @param message 错误信息。
     * @param cause   根本原因，通常是另一个异常，表示引发当前异常的根本原因。
     */
    public KitchenException(String message, Throwable cause) {
        super(message, cause);
    }
}
