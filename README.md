# SwitchView
一个自定义Switch Button View

2018-05-17 11:47 update

How to
To get a Git project into your build:
Step 1. Add the JitPack repository to your build file
gradle
maven
sbt
leiningen
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}Copy
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.chengdahong:SwitchView:V1.0'
	}
	
	
2018-05-17 14:48 更新
发布v1.0.1版本
更改内容:
1.添加动态设置on、off文字功能
2.解决文字居中问题