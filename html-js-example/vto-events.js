// VTO SDK Event Subscriptions
function subscribeToVTOEvents() {
  pulpoar.onReady(payload => {
    console.log('VTO ready:', payload)
    logEvent('onReady', payload)
  })

  pulpoar.onError(error => logEvent('onError', error))
  pulpoar.onPathChange(payload => logEvent('onPathChange', payload))
  pulpoar.onGoBack(() => logEvent('onGoBack', {}))

  pulpoar.onGdprApprove(payload => logEvent('onGdprApprove', payload))
  pulpoar.onGdprLinkClick(payload => logEvent('onGdprLinkClick', payload))

  pulpoar.onUploadPhoto(() => logEvent('onUploadPhoto', {}))
  pulpoar.onModelSelect(payload => logEvent('onModelSelect', payload))
  pulpoar.onTakePhoto(() => logEvent('onTakePhoto', {}))
  pulpoar.onUsePhoto(() => logEvent('onUsePhoto', {}))
  pulpoar.onTakePhotoAgain(() => logEvent('onTakePhotoAgain', {}))

  pulpoar.onTryNowClick(payload => logEvent('onTryNowClick', payload))
  pulpoar.onCameraMirror(payload => logEvent('onCameraMirror', payload))
  pulpoar.onCameraSwitch(payload => logEvent('onCameraSwitch', payload))

  pulpoar.onCurtainToggle(payload => logEvent('onCurtainToggle', payload))
  pulpoar.onCurtainSlideStart(payload => logEvent('onCurtainSlideStart', payload))
  pulpoar.onCurtainSlideEnd(payload => logEvent('onCurtainSlideEnd', payload))
  pulpoar.onOpacitySlideStart(payload => logEvent('onOpacitySlideStart', payload))
  pulpoar.onOpacitySlideEnd(payload => logEvent('onOpacitySlideEnd', payload))
  pulpoar.onZoom(payload => logEvent('onZoom', payload))

  pulpoar.onExperienceSelect(payload => logEvent('onExperienceSelect', payload))

  pulpoar.onLookSelect(payload => logEvent('onLookSelect', payload))
  pulpoar.onBrandSelect(payload => logEvent('onBrandSelect', payload))
  pulpoar.onCategorySelect(payload => logEvent('onCategorySelect', payload))
  pulpoar.onProductSelect(payload => logEvent('onProductSelect', payload))
  pulpoar.onVariantSelect(payload => logEvent('onVariantSelect', payload))
  pulpoar.onAppliedVariantsChange(payload => logEvent('onAppliedVariantsChange', payload))

  pulpoar.onAddToCart(payload => {
    console.log('Products added to cart:', payload)
    logEvent('onAddToCart', payload)
  })
  pulpoar.onGoToProduct(payload => logEvent('onGoToProduct', payload))
}
