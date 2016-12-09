# Easy-Download
Easy-Download是一个简单易用的后台下载框架，支持断点下载，通过通知显示下载进度和状况

####如何导入：
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.biloba123:Easy-Download:v1.0'
	}
  
####如何使用：
 
 在要使用的Activity中加上以下声明
 
    private DownloadService.DownloadBinder mDownloadBinder;
    
    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mDownloadBinder= (DownloadService.DownloadBinder) binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    
  在onCreate或其他方法中做好准备工作：
  
      DownloadService.prepare(this, mConnection);
      
  下载对应的相关方法：
  
    mDownloadBinder.startDownload(String url);//开始下载指定地址文件
    mDownloadBinder.pauseDownload();//暂停下载
    mDownloadBinder.cancelDownload();//取消下载并删除已下载文件
    
   最后在onDestory中解除服务：
   
     unbindService( mConnection);
  
