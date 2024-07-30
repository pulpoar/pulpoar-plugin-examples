import WebKit

// Function to get the event script
func getEventScript(events: Events) -> String {
    let usedEvents = getUsedProperties(of: events)
    let script = """
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/@pulpoar/plugin-sdk@latest/dist/index.iife.js';
    script.onload = function() {
    \(usedEvents)
    window.pulpoar = pulpoar;
    }
    document.body.appendChild(script);
    """
    return script
}

var eventHandlers = [
    "onReady": PulpoARSDK.onReady(),
    "onError":PulpoARSDK.onError(),
    "onPathChange":PulpoARSDK.onPathChange(),
    "onTryNowClick":PulpoARSDK.onTryNowClick(),
    "onGdprApprove":PulpoARSDK.onGdprApprove(),
    "onUploadPhoto":PulpoARSDK.onUploadPhoto(),
    "onModelSelect":PulpoARSDK.onModelSelect(),
    "onTakePhoto":PulpoARSDK.onTakePhoto(),
    "onCurtainSlideStart":PulpoARSDK.onTakePhoto(),
    "onCurtainToggle":PulpoARSDK.onCurtainToggle(),
    "onUsePhoto":PulpoARSDK.onUsePhoto(),
    "onTakePhotoAgain":PulpoARSDK.onTakePhotoAgain(),
    "onCurtainSlideEnd":PulpoARSDK.onCurtainSlideEnd(),
    "onOpacitySlideStart":PulpoARSDK.onOpacitySlideStart(),
    "onOpacitySlideEnd": PulpoARSDK.onOpacitySlideEnd(),
    "onZoom": PulpoARSDK.onZoom(),
    "onCameraMirror":PulpoARSDK.onCameraMirror(),
    "onExperienceSelect":PulpoARSDK.onExperienceSelect(),
    "onLookSelect":PulpoARSDK.onLookSelect(),
    "onGoBack":PulpoARSDK.onGoBack(),
    "onBrandSelect":PulpoARSDK.onBrandSelect(),
    "onCategorySelect": PulpoARSDK.onCategorySelect(),
    "onProductSelect":PulpoARSDK.onProductSelect(),
    "onVariantSelect": PulpoARSDK.onVariantSelect(),
    "onAppliedVariantsChange":PulpoARSDK.onAppliedVariantsChange(),
    "onAddToCart":PulpoARSDK.onAddToCart(),
    "onGoToProduct":PulpoARSDK.onGoToProduct()
]

func getUsedProperties<T>(of instance: T) -> String {
    let mirror = Mirror(reflecting: instance)
    var usedProperties: [String] = []
    
    for child in mirror.children {
        if let propertyName = child.label, let value = child.value as? Any? {
            if value != nil {
                usedProperties.append(eventHandlers[propertyName]!)
            }
        }
    }
    
    return usedProperties.joined(separator: "\n")
}

// Function to build SDK event scripts
func sdkEventBuilder(_ funcName: String) -> String {
    return """
    pulpoar['\(funcName)']((data)=>{
       window.webkit.messageHandlers.eventHandler.postMessage({event: "\(funcName)", data: data});
    });
    """
}

// Type guard for event types
func isEventType(_ key: String) -> Bool {
    return eventHandlers[key] != nil
}


func getEventHandler(events: Events) -> [String: (Any) -> Void] {
    return [
        "onReady": { data in
            if let onReady = events.onReady {
                let dict = data as? [String: Any]
                let brandDict = dict?["brands"] as? [[String: Any]]
                let categoryDict = dict?["categories"] as? [[String: Any]]
                let productDict = dict?["products"] as? [[String: Any]]
                let variantDict = dict?["variants"] as?  [[String: Any]]
                let brands = brandDict?.compactMap({brandData -> Brand? in

                    return Brand(brandData)
                })
                
                let categories = categoryDict?.compactMap({categoryData -> Category? in
                    return Category(categoryData)
                })
                
                let products = productDict?.compactMap({productData -> Product? in
                    let productCategory = productData["category"] as! [String: Any]
                    let productBrand = productData["brand"] as! [String: Any]
                    return Product(productData,brand: productBrand, category: productCategory)
                })
                let variants = variantDict?.compactMap({variantData -> Variant? in
                    
                    return Variant(variantData, product: variantData["product"] as! [String: Any])
                })
                onReady(ApplicationData(brands: brands ?? [], categories: categories ?? [], module: dict?["module"] as! String, products: products ?? [], projectId: dict?["projectId"] as! String, themeType: dict?["themeType"] as! String, variants: variants ?? []))
            }
        },
        
        "onError": { data in
            if let onError = events.onError {
                onError(data)
            }
        },
        
        "onPathChange": { data in
            if let onPathChange = events.onPathChange {
                let dict = data as? [String: Any]
                onPathChange(PathChangePayload(path: (dict!["path"] as? String)!, referer: (dict!["referer"] as? String)!))
            }
        },
        "onTryNowClick": { data in
            let dict = data as? [String: Any]
            if let onTryNowClick = events.onTryNowClick {
                onTryNowClick(TryNowClickPayload(fallback: (dict?["fallback"] as? String)!))
            }
        },
        "onGdprApprove": { data in
            let dict = data as? [String: Any]
            if let onGdprApprove = events.onGdprApprove {
                onGdprApprove(GDPRApprovePayload(approved: (dict?["approved"] as? Bool)!))
            }
        },
        "onUploadPhoto": { data in
            if let onUploadPhoto = events.onUploadPhoto {
                onUploadPhoto(data)
            }
        },
        "onModelSelect": { data in
            let dict = data as? [String: Any]
            if let onModelSelect = events.onModelSelect {
                onModelSelect(ModelSelectPayload(id: (dict!["id"] as? Int)!, image: (dict!["image"] as? String)!))
            }
        },
        "onTakePhoto": { data in
            if let onTakePhoto =  events.onTakePhoto {
                let dict = data as? [String: Any]
                onTakePhoto(data)
            }
        },
        "onCurtainSlideStart": { data in
            let dict = data as? [String: Any]
            if let onCurtainSlideStart =  events.onCurtainSlideStart {
                onCurtainSlideStart(CurtainSliderPayload(posX: (dict?["posX"] as? Double)!))
            }
        },
        "onCurtainToggle": { data in
            let dict = data as? [String: Any]
            if let onCurtainToggle =  events.onCurtainToggle {
                onCurtainToggle(CurtainTogglePayload(active: (dict?["posX"] as? Bool)!))
            }
        },
        "onUsePhoto": { data in
            if let onUsePhoto =  events.onUsePhoto {
                onUsePhoto(data)
            }
        },
        "onTakePhotoAgain": { data in
            if let onTakePhotoAgain =  events.onTakePhotoAgain {
                onTakePhotoAgain(data)
            }
        },
        "onCurtainSlideEnd": { data in
            let dict = data as? [String: Any]

            if let onCurtainSlideEnd =  events.onCurtainSlideEnd {
                onCurtainSlideEnd(CurtainSliderPayload(posX: (dict?["posX"] as? Double)!))
            }
        },
        "onOpacitySlideStart": { data in
            let dict = data as? [String: Any]

            if let onOpacitySlideStart =  events.onOpacitySlideStart {
                onOpacitySlideStart(OpacitySlidePayload(percentage: (dict?["percentage"] as? Double)!))
            }
        },
        "onOpacitySlideEnd": { data in
            let dict = data as? [String: Any]
            if let onOpacitySlideEnd =  events.onOpacitySlideEnd {
                onOpacitySlideEnd(OpacitySlidePayload(percentage: (dict?["percentage"] as? Double)!))
            }
        },
        "onZoom": { data in
            let dict = data as? [String: Any]

            if let onZoom =  events.onZoom {
                onZoom(ZoomPayload(type: (dict!["type"] as? String)!, value: (dict!["value"] as? Double)!))
            }
        },
        "onCameraMirror": { data in
            if let onCameraMirror =  events.onCameraMirror {
                let dict = data as? [String: Any]

                onCameraMirror(CameraMirrorPayload(activate: (dict!["activate"] as? Bool)!))
            }
        },
        "onExperienceSelect": { data in
            let dict = data as? [String: Any]

            if let onExperienceSelect =  events.onExperienceSelect {
                onExperienceSelect(ExperienceSelectPayload(type: (dict!["type"] as? String)!))
            }
        },
        "onLookSelect": { data in
            let dict = data as? [String: Any]
            
            if let onLookSelect =  events.onLookSelect {
                onLookSelect(LookSelectPayload(dict!,variants: getVariantList(variantDict: dict?["variants"] as? [[String : Any]] ?? [])))
            }
        },
        "onGoBack": { data in
            if let onGoBack =  events.onGoBack {
                onGoBack(data)
            }
        },
        "onBrandSelect": { data in
            let dict = data as? [String: Any]

            if let onBrandSelect =  events.onBrandSelect {
                onBrandSelect(Brand(dict!))
            }
        },
        "onCategorySelect": { data in
            let dict = data as? [String: Any]

            if let onCategorySelect =  events.onCategorySelect {
                onCategorySelect(Category(dict!))
            }
        },
        "onProductSelect": { data in
            let dict = data as? [String: Any]
            let productCategory = dict!["category"] as! [String: Any]
            let productBrand = dict!["brand"] as! [String: Any]
            
            if let onProductSelect =  events.onProductSelect {
                onProductSelect(Product(dict!,brand: productBrand, category: productCategory))
            }
        },
        "onVariantSelect": { data in
            let dict = data as? [String: Any]
            if let onVariantSelect =  events.onVariantSelect {
                onVariantSelect(Variant(dict!, product: dict?["product"] as! [String : Any]))
            }
        },
        "onAppliedVariantsChange": { data in
            let dict = data as? [String: Any]
            let variantDict = dict?["variants"] as?  [[String: Any]]
            
            let variants = variantDict?.compactMap({variantData -> Variant? in
                
                return Variant(variantData, product: variantData["product"] as! [String: Any])
            })
            if let onAppliedVariantsChange =  events.onAppliedVariantsChange {
                onAppliedVariantsChange(AppliedVariantsChangePayload(triggererVariantId: dict?["triggererVariantId"] as! String, variants: variants ?? []))
            }
        },
        "onAddToCart": { data in
            if let onAddToCart =  events.onAddToCart {
                let arr = data as? [[String:Any]]
                let payload = arr?.compactMap({productData -> AddToCartPayload? in
                    return AddToCartPayload(productData , product: productData["product"] as! [String:Any])
                })
                onAddToCart(payload ?? [])
            }
        },
        "onGoToProduct": { data in
            if let onGoToProduct =  events.onGoToProduct {
                onGoToProduct(data)
            }
        }
    ]
}
func getVariantList (variantDict: [[String:Any]]) -> [Variant]{
    return variantDict.compactMap({variantData -> Variant? in
        
        return Variant(variantData, product: variantData["product"] as! [String: Any])
    })
}
