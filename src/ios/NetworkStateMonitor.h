#import <Cordova/CDV.h>

@interface NetworkStateMonitor : CDVPlugin

- (void)pluginInitialize;
- (void)registerCallback:(CDVInvokedUrlCommand*)command;
- (void)unregisterCallback:(CDVInvokedUrlCommand*)command;
- (void)getState:(CDVInvokedUrlCommand*)command;
- (void)dealloc;

@end