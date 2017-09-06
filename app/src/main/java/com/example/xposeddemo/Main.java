package com.example.xposeddemo;

import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by Mick on 2017/9/4.
 */

public class Main implements IXposedHookLoadPackage, IXposedHookInitPackageResources {
    public final static String TAG = "XposedLog";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 将包名不是 com.example.login 的应用剔除掉
        if (!loadPackageParam.packageName.equals("com.yzqs.login"))
            return;
        XposedBridge.log("Loaded app: " + loadPackageParam.packageName);

        // Hook MainActivity中的isCorrectInfo(String,String)方法
        findAndHookMethod("com.yzqs.login.MainActivity", loadPackageParam.classLoader, "isCorrectInfo", String.class,
                String.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "开始劫持了~");
                        Log.d(TAG, "用户名 = 》》" + param.args[0]);
                        Log.d(TAG, "密码 = 》》" + param.args[1]);
                    }

                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        Log.d(TAG, "劫持结束了~");
                        Log.d(TAG, "用户名 =》》 " + param.args[0]);
                        Log.d(TAG, "密码 =》》 " + param.args[1]);
                    }
                });

        /**
         * 劫持IMEI号修改
         */
        findAndHookMethod(TelephonyManager.class, "getDeviceId", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return "5201314";
            }
        });


        final Class<?> clazz = XposedHelpers.findClass("com.yzqs.login.HookDemo", loadPackageParam.classLoader);
        //getClassInfo(clazz);

        //不需要获取类对象，即可直接修改类中的私有静态变量staticInt
        XposedHelpers.setStaticIntField(clazz, "staticInt", 99);

        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookConstructor(clazz, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = "Haha, HookDemo(str) are hooked";
            }
        });

        //Hook有参构造函数，修改参数------不能使用XC_MethodReplacement()替换构造函数内容，
        //XposedHelpers.findAndHookConstructor(clazz, String.class, new XC_MethodReplacement() {
        //    @Override
        //    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        //        Log.d("HookDemo" , "HookDemo(str) was replace");
        //    }
        //});

        //Hook公有方法publicFunc，
        // 1、修改参数
        // 2、修改下publicInt和privateInt的值
        // 3、再顺便调用一下隐藏函数hideFunc
        //XposedHelpers.findAndHookMethod("com.example.xposedhooktarget.HookDemo", clazz.getClassLoader(), "publicFunc", String.class, new XC_MethodHook()
        XposedHelpers.findAndHookMethod(clazz, "publicFunc", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = "Haha, publicFunc are hooked";
                XposedHelpers.setIntField(param.thisObject, "publicInt", 199);
                XposedHelpers.setIntField(param.thisObject, "privateInt", 299);
                // 让hook的对象本身去执行流程
                Method md = clazz.getDeclaredMethod("hideFunc", String.class);
                md.setAccessible(true);
                //md.invoke(param.thisObject, "Haha, hideFunc was hooked");
                XposedHelpers.callMethod(param.thisObject, "hideFunc", "Haha, hideFunc was hooked");

                //实例化对象，然后再调用HideFunc方法 
                //Constructor constructor = clazz.getConstructor();
                //XposedHelpers.callMethod(constructor.newInstance(), "hideFunc", "Haha, hideFunc was hooked");
            }
        });

        //Hook私有方法privateFunc，修改参数
        //XposedHelpers.findAndHookMethod("com.example.xposedhooktarget.HookDemo", clazz.getClassLoader(), "privateFunc", String.class, new XC_MethodHook()
        XposedHelpers.findAndHookMethod(clazz, "privateFunc", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = "Haha, privateFunc are hooked";
            }
        });

        //Hook私有静态方法staticPrivateFunc, 修改参数
        XposedHelpers.findAndHookMethod(clazz, "staticPrivateFunc", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = "Haha, staticPrivateFunc are hooked";
            }
        });

        //Hook复杂参数函数complexParameterFunc
        Class fclass1 = XposedHelpers.findClass("java.util.Map", loadPackageParam.classLoader);
        Class fclass2 = XposedHelpers.findClass("java.util.ArrayList", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(clazz, "complexParameterFunc", String.class,
                "[[Ljava.lang.String;", fclass1, fclass2, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = "Haha, complexParameterFunc are hooked";
                    }
                });

        //Hook私有方法repleaceFunc, 替换打印内容
        XposedHelpers.findAndHookMethod(clazz, "repleaceFunc", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.d("HookDemo", "Haha, repleaceFunc are replaced");
                return null;
            }
        });
//        //Hook方法, anonymousInner， 参数是抽象类，先加载所需要的类即可
//        Class animalClazz = loadPackageParam.classLoader.loadClass("com.example.xposedhooktarget.Animal");
//        XposedHelpers.findAndHookMethod(clazz, "anonymousInner", animalClazz, String.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("HookDemo This is test");
//                param.args[1] = "Haha, anonymousInner are hooked";
//            }
//        });

        //Hook匿名类的eatFunc方法，修改参数，顺便修改类中的anonymoutInt变量
        XposedHelpers.findAndHookMethod("com.yzqs.login.HookDemo$1", clazz.getClassLoader(),
                "eatFunc", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = "Haha, eatFunc are hooked";
                        XposedHelpers.setIntField(param.thisObject, "anonymoutInt", 499);
                    }
                });

        //hook内部类的构造方法失败，且会导致hook内部类的InnerFunc方法也失败，原因不明
//            XposedHelpers.findAndHookConstructor(clazz1, new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            XposedBridge.log("Haha, InnerClass constructed was hooked" );
//                        }
//                    });

        //Hook内部类InnerClass的InnerFunc方法，修改参数，顺便修改类中的innerPublicInt和innerPrivateInt变量
        final Class<?> clazz1 = XposedHelpers.findClass("com.yzqs.login.HookDemo$InnerClass", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(clazz1, "InnerFunc", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = "Haha, InnerFunc was hooked";
                XposedHelpers.setIntField(param.thisObject, "innerPublicInt", 9);
                XposedHelpers.setIntField(param.thisObject, "innerPrivateInt", 19);
            }
        });
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.yzqs.login")) {
            return;
        }

        resparam.res.hookLayout("com.yzqs.login", "layout", "activity_main", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                try {
                    Button view = liparam.view.findViewById(liparam.res.getIdentifier("login", "id", "com.yzqs.login"));
                    view.setTextColor(Color.RED);
                    Log.d(TAG, view.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


