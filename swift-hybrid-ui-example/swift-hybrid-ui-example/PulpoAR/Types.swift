import Foundation

struct PathChangePayload: Codable {
    let path: String
    let referer: String
    init(path: String?, referer: String?) {
        self.path = path!
        self.referer = referer!
    }
}

struct AddToCartPayload: Codable {
    let barcode: String?
    let config: Config?
    let id: String?
    let image: String?
    let name: String?
    let product: Product
    let slug: String?
    let thumbnailColor: String?
    let thumbnailImage: String?
    let translations: [Translation]
    let webLink: String?
    init(_ object:[String: Any], product: [String: Any]) {
        self.barcode = object["barcode"] as? String
        self.config = Config(object["config"] as! [String:Any])
        self.id = object["id"] as? String
        self.image = object["image"] as? String
        self.name = object["name"] as? String
        self.product = Product(product, brand: product["brand"] as! [String:Any], category: product["category"] as! [String:Any])
        self.slug = object["slug"] as? String
        self.thumbnailColor = object["thumbnail_color"] as? String
        self.thumbnailImage = object["thumbnail_image"] as? String
        self.translations = []
        self.webLink = object["web_link"] as? String
    }
}

struct Config: Codable {
    var config: ConfigDetails?
    var module: String?
    init(_ object:[String: Any]) {
        self.config = ConfigDetails(object["config"] as! [String:Any])
        self.module = object["module"] as? String
    }
}

struct ConfigDetails: Codable {
    var colors: [ContentColor]?
    var config: ConfigSettings?
    var opacityMultiplier: Int?
    var subType: Int?
    var textureIdsToApply: [Texture]?
    var textureIdsToFetch: [Texture]?
    var type: Int?
    
    init(_ object:[String: Any]) {
        self.colors = (object["colors"] as! [[String: Any]]).map({colorInfo -> ContentColor in
            return ContentColor(colorInfo )})
      
        self.opacityMultiplier = object["opacity_multiplier"] as? Int
        self.subType = object["sub_type"] as? Int
        self.textureIdsToApply = (object["texture_ids_to_apply"] as! [[String: Any]]).map({textureInfo -> Texture in
            return Texture(textureInfo )})
        self.textureIdsToFetch = (object["texture_ids_to_fetch"] as! [[String: Any]]).map({textureInfo -> Texture in
            return Texture(textureInfo )})
        self.type = object["type"] as? Int
        guard let configData = object["config"] as? [String: Any] else {
            self.config = ConfigSettings([:])
            return 
        }
        self.config = ConfigSettings(configData)
    }
}


struct ConfigSettings: Codable {
    var darknessOpacity: String?
    var darknessSmooth: Int?
    var lightnessOpacity: String?
    var lightnessSmooth: Int?
    var lipstickLookType: String?
    var mascaraType: Int?
    var coverageRate: String?
    init(_ object:[String: Any]) {
        self.darknessOpacity = object["darkness_opacity"] as? String ?? ""
        self.darknessSmooth = object["darkness_smooth"] as? Int ?? 0
        self.lightnessOpacity = object["lightness_opacity"] as? String ?? ""
        self.lightnessSmooth = object["lightness_smooth"] as? Int ?? 0
        self.lipstickLookType = object["lipsticklook_type"] as? String ?? ""
        self.mascaraType = object["mascara_type"] as? Int ?? 0
        self.coverageRate = object["coverage_rate"] as? String ?? ""
    }
}

struct Texture: Codable {
    var id: String?
    var url: String?
    init(_ object:[String: Any]) {
        self.id = object["id"] as? String
        self.url = object["url"] as? String
    }
}

struct AppliedVariantsChangePayload: Codable {
    let triggererVariantId: String
    let variants: [Variant]
    init(triggererVariantId: String, variants: [Variant]) {
        self.triggererVariantId = triggererVariantId
        self.variants = variants
    }
}

struct ContentColor: Codable {
    let blend: Int
    let color: String
    let opacity: String
    init(_ object:[String: Any]) {
        self.blend = (object["blend"] as? Int)!
        self.color = (object["color"] as? String)!
        self.opacity = (object["color"] as? String)!
    }
}

struct ModelSelectPayload: Codable {
    let id: Int
    let image: String
}

struct ZoomPayload: Codable {
    let type: String
    let value: Double
}

struct GDPRApprovePayload: Codable {
    let approved: Bool
}

struct TryNowClickPayload: Codable {
    let fallback: String
}

struct OpacitySlidePayload: Codable {
    let percentage: Double
}

struct CameraMirrorPayload: Codable {
    let activate: Bool
}


struct ProductSelectPayload: Codable {
    let brand: Brand
    let category: Category
    let id: String
    let image: String
    let name: String
    let slug: String?
    let translations: [Translation]
    let variants: [Variant]
}

struct CurtainSliderPayload: Codable {
    let posX: Double
}

struct CurtainTogglePayload: Codable {
    let active: Bool
}

struct ExperienceSelectPayload: Codable {
    let type: String
}

struct LookSelectPayload: Codable {
    let id: String
    let image: String
    let name: String
    let translations: [Translation]
    let variants: [Variant]
    init(_ object:[String: Any], variants: [Variant]) {
        self.id = object["id"] as! String
        self.image = object["image"] as! String
        self.name = object["name"] as! String
        self.translations = []
        self.variants = variants
    }
}

struct ApplicationData: Codable {
    let brands: [Brand]
    let categories: [Category]
    let module: String
    let products: [Product]
    let projectId: String
    let themeType: String
    let variants: [Variant]
    init(brands: [Brand], categories: [Category], module: String, products: [Product], projectId: String, themeType: String, variants: [Variant]) {
        self.brands = brands
        self.categories = categories
        self.module = module
        self.products = products
        self.projectId = projectId
        self.themeType = themeType
        self.variants = variants
    }
}

struct Translation: Codable {
    let description: String
    let id: Int
    let languagesCode: String
    let name: String
    let vtoCategoriesId: String
    init(description: String, id: Int, languagesCode: String, name: String, vtoCategoriesId: String) {
        self.description = description
        self.id = id
        self.languagesCode = languagesCode
        self.name = name
        self.vtoCategoriesId = vtoCategoriesId
    }
}

struct Category: Codable {
    let id: String
    let image: String
    let name: String
    let slug: String?
    let translations: [Translation]
    init(_ object:[String: Any]) {
        self.id = object["id"] as! String
        self.image = object["image"] as! String
        self.name = object["name"] as! String
        self.slug = object["slug"] as? String
        self.translations = []
    }
}

struct Brand: Codable {
    let id: String
    let image: String
    let name: String
    let slug: String?
    let translations: [Translation]
    init(_ object:[String: Any]){
        
        self.id = object["id"] as! String
        self.image = object["image"] as! String
        self.name = object["name"]  as! String
        self.slug = object["slug"] as? String
        self.translations = []
    }
}

struct Product: Codable {
    let brand: Brand
    let category: Category
    let id: String
    let image: String
    let name: String
    let slug: String?
    let translations: [Translation]
    init(_ object: [String: Any], brand: [String: Any], category: [String: Any]) {
        self.brand = Brand(brand)
        self.category = Category(category)
        self.id = object["id"] as! String
        self.image = object["image"] as! String
        self.name = object["name"] as! String
        self.slug = object["slug"] as? String
        self.translations = []
    }
}

struct Variant: Codable {
    let barcode: String?
    let config: Config
    let id: String
    let image: String?
    let name: String
    let product: Product
    let slug: String?
    let thumbnailColor: String?
    let thumbnailImage: String?
    let translations: [Translation]
    let webLink: String?
    
    init(_ object: [String: Any], product: [String: Any]) {
        self.id = object["id"] as! String
        self.image = object["image"] as? String
        self.name = object["name"] as! String
        self.slug = object["slug"] as? String
        self.barcode = object["barcode"] as? String
        self.product = Product(product,brand: product["brand"] as! [String: Any] ,category: product["category"] as! [String: Any])
        self.thumbnailColor = object["thumbnail_color"] as? String
        self.thumbnailImage = object["thumbnail_image"] as? String
        self.translations = []
        self.webLink = object["web_link"] as? String
        self.config = Config(object["config"] as! [String:Any])
    }
}

struct Events {
    let onAddToCart: (([AddToCartPayload]) -> Void)?
    let onPathChange: ((PathChangePayload) -> Void)?
    let onAppliedVariantsChange: ((AppliedVariantsChangePayload) -> Void)?
    let onVariantSelect: ((Variant) -> Void)?
    let onBrandSelect: ((Brand) -> Void)?
    let onCameraMirror: ((CameraMirrorPayload) -> Void)?
    let onExperienceSelect: ((ExperienceSelectPayload) -> Void)?
    let onLookSelect: ((LookSelectPayload) -> Void)?
    let onGoBack: ((Any) -> Void)?
    let onCategorySelect: ((Category) -> Void)?
    let onProductSelect: ((Product) -> Void)?
    let onCurtainSlideEnd: ((CurtainSliderPayload) -> Void)?
    let onCurtainSlideStart: ((CurtainSliderPayload) -> Void)?
    let onOpacitySlideStart: ((OpacitySlidePayload) -> Void)?
    let onOpacitySlideEnd: ((OpacitySlidePayload) -> Void)?
    let onZoom: ((ZoomPayload) -> Void)?
    let onCurtainToggle: ((CurtainTogglePayload) -> Void)?
    let onTryNowClick: ((TryNowClickPayload) -> Void)?
    let onGdprApprove: ((GDPRApprovePayload) -> Void)?
    let onUploadPhoto: ((Any) -> Void)?
    let onGoToProduct: ((Any) -> Void)?
    let onModelSelect: ((ModelSelectPayload) -> Void)?
    let onTakePhotoAgain: ((Any) -> Void)?
    let onUsePhoto: ((Any) -> Void)?
    let onError: ((Any) -> Void)?
    let onReady: ((ApplicationData) -> Void)?
    let onTakePhoto: ((Any) -> Void)?

    init(
        onAddToCart: (([AddToCartPayload]) -> Void)? = nil,
        onPathChange: ((PathChangePayload) -> Void)? = nil,
        onAppliedVariantsChange: ((AppliedVariantsChangePayload) -> Void)? = nil,
        onVariantSelect: ((Variant) -> Void)? = nil,
        onBrandSelect: ((Brand) -> Void)? = nil,
        onCameraMirror: ((CameraMirrorPayload) -> Void)? = nil,
        onExperienceSelect: ((ExperienceSelectPayload) -> Void)? = nil,
        onLookSelect: ((LookSelectPayload) -> Void)? = nil,
        onGoBack: ((Any) -> Void)? = nil,
        onCategorySelect: ((Category) -> Void)? = nil,
        onProductSelect: ((Product) -> Void)? = nil,
        onCurtainSlideEnd: ((CurtainSliderPayload) -> Void)? = nil,
        onCurtainSlideStart: ((CurtainSliderPayload) -> Void)? = nil,
        onOpacitySlideStart: ((OpacitySlidePayload) -> Void)? = nil,
        onOpacitySlideEnd: ((OpacitySlidePayload) -> Void)? = nil,
        onZoom: ((ZoomPayload) -> Void)? = nil,
        onCurtainToggle: ((CurtainTogglePayload) -> Void)? = nil,
        onTryNowClick: ((TryNowClickPayload) -> Void)? = nil,
        onGdprApprove: ((GDPRApprovePayload) -> Void)? = nil,
        onUploadPhoto: ((Any) -> Void)? = nil,
        onGoToProduct: ((Any) -> Void)? = nil,
        onModelSelect: ((ModelSelectPayload) -> Void)? = nil,
        onTakePhotoAgain: ((Any) -> Void)? = nil,
        onUsePhoto: ((Any) -> Void)? = nil,
        onError: ((Any) -> Void)? = nil,
        onReady: ((ApplicationData) -> Void)? = nil,
        onTakePhoto: ((Any) -> Void)? = nil
    ) {
        self.onAddToCart = onAddToCart
        self.onPathChange = onPathChange
        self.onAppliedVariantsChange = onAppliedVariantsChange
        self.onVariantSelect = onVariantSelect
        self.onBrandSelect = onBrandSelect
        self.onCameraMirror = onCameraMirror
        self.onExperienceSelect = onExperienceSelect
        self.onLookSelect = onLookSelect
        self.onGoBack = onGoBack
        self.onCategorySelect = onCategorySelect
        self.onProductSelect = onProductSelect
        self.onCurtainSlideEnd = onCurtainSlideEnd
        self.onCurtainSlideStart = onCurtainSlideStart
        self.onOpacitySlideStart = onOpacitySlideStart
        self.onOpacitySlideEnd = onOpacitySlideEnd
        self.onZoom = onZoom
        self.onCurtainToggle = onCurtainToggle
        self.onTryNowClick = onTryNowClick
        self.onGdprApprove = onGdprApprove
        self.onUploadPhoto = onUploadPhoto
        self.onGoToProduct = onGoToProduct
        self.onModelSelect = onModelSelect
        self.onTakePhotoAgain = onTakePhotoAgain
        self.onUsePhoto = onUsePhoto
        self.onError = onError
        self.onReady = onReady
        self.onTakePhoto = onTakePhoto
    }
}

struct PulpoARProps {
    let plugin: String
    let slug: String
    let events: Events

    init(
        plugin: String,
        slug: String,
        events: Events
    ) {
        self.plugin = plugin
        self.slug = slug
        self.events = events
    }
}
