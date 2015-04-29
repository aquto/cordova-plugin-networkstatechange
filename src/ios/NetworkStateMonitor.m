#import <Foundation/Foundation.h>

#import "NetworkStateMonitor.h"
#import "Reachability.h"
#import <Cordova/CDV.h>


@interface NetworkStateMonitor () {
    Reachability *reachability;
    NetworkStatus prevState;
}

@end

@implementation NetworkStateMonitor

- (void)pluginInitialize {
    NSString *hostname = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"ReachabilityHostname"];
    reachability = [Reachability reachabilityWithHostname:hostname];
}

- (NSString *)statusToString:(NetworkStatus)status {
    switch(status) {
        case NotReachable:
            return @"NONE";
        case ReachableViaWiFi:
            return @"WIFI";
        case ReachableViaWWAN:
            return @"MOBILE";
        default:
            return @"UNKNOWN";
    }
}

- (void)registerCallback:(CDVInvokedUrlCommand*)command {
    NSString* localCallbackId = command.callbackId;
    [self.commandDelegate runInBackground:^{
        prevState = [reachability currentReachabilityStatus];
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [[NSNotificationCenter defaultCenter] addObserverForName:kReachabilityChangedNotification object:nil 
                                              queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification *notif) {
            Reachability *r = [notif object];
            NetworkStatus status = [r currentReachabilityStatus];
            if(status == prevState)
                return;
            NSString *netType = [self statusToString:status];
            prevState = status;
            NSLog(@"Network transitioned to %@", netType);

            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:netType];
            [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:localCallbackId];
        }];
        [reachability startNotifier];
        NSLog(@"Registered callback");

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:localCallbackId];
    }];
}

- (void)unregisterCallback:(CDVInvokedUrlCommand*)command {
    NSString* localCallbackId = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [reachability stopNotifier];
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        NSLog(@"Unregistered callback");

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:localCallbackId];
    }];
}

- (void)getState:(CDVInvokedUrlCommand*)command {
    NSString* localCallbackId = command.callbackId;
    [self.commandDelegate runInBackground:^{
        NetworkStatus status = [reachability currentReachabilityStatus];
        NSString *netType = [self statusToString:status];
        NSLog(@"Network is %@", netType);

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:netType];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:localCallbackId];
    }];
}

- (void)dealloc {
    [reachability stopNotifier];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end