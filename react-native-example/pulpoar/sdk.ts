import type {
  Variant,
  AddToCartPayload,
  AppliedVariantsChangePayload,
  BrandSelectPayload,
  CameraMirrorPayload,
  CategorySelectPayload,
  CurtainSlidePayload,
  CurtainTogglePayload,
  ExperienceSelectPayload,
  GDPRApprovePayload,
  GoToProductPayload,
  LookSelectPayload,
  ModelSelectPayload,
  OpacitySlidePayload,
  PathChangePayload,
  ProductSelectPayload,
  ProjectData,
  TryNowClickPayload,
  VariantsChangePayload,
  ZoomPayload,
} from './types';

const setPath = (
  path: 'root' | 'choose-model' | 'take-photo' | 'apply-live' | 'apply-photo',
) => `pulpoar.setPath("${path}")`;

const applyVariants = (array: Variant['slug'][]) =>
  `pulpoar.applyVariants(${array})`;

const setImageToApply = (image: string) =>
  `pulpoar.setImageToApply("${image}")`;

const initCamera = (mode: {facingMode: 'user' | 'environment'}) =>
  `pulpoar.initCamera(${mode})`;

const onReady = (_payload: ProjectData): void => undefined;

const onError = (_payload: any): void => undefined;

const onPathChange = (_payload: PathChangePayload): void => undefined;

const onTryNowClick = (_payload: TryNowClickPayload): void => undefined;

const onGdprApprove = (_payload: GDPRApprovePayload): void => undefined;

const onUploadPhoto = (_payload: undefined): void => undefined;

const onModelSelect = (_payload: ModelSelectPayload): void => undefined;

const onTakePhoto = (_payload: undefined): void => undefined;

const onUsePhoto = (_payload: undefined): void => undefined;

const onTakePhotoAgain = (_payload: undefined): void => undefined;

const onCurtainToggle = (_payload: CurtainTogglePayload): void => undefined;

const onCurtainSlideStart = (_payload: CurtainSlidePayload): void => undefined;

const onCurtainSlideEnd = (_payload: CurtainSlidePayload): void => undefined;

const onOpacitySlideStart = (_payload: OpacitySlidePayload): void => undefined;

const onOpacitySlideEnd = (_payload: OpacitySlidePayload): void => undefined;

const onZoom = (_payload: ZoomPayload): void => undefined;

const onCameraMirror = (_payload: CameraMirrorPayload): void => undefined;

const onExperienceSelect = (_payload: ExperienceSelectPayload): void =>
  undefined;

const onLookSelect = (_payload: LookSelectPayload): void => undefined;

const onGoBack = (_payload: undefined): void => undefined;

const onBrandSelect = (_payload: BrandSelectPayload): void => undefined;

const onCategorySelect = (_payload: CategorySelectPayload): void => undefined;

const onProductSelect = (_payload: ProductSelectPayload): void => undefined;

const onVariantSelect = (_payload: VariantsChangePayload): void => undefined;

const onAppliedVariantsChange = (
  _payload: AppliedVariantsChangePayload,
): void => undefined;

const onAddToCart = (_payload: AddToCartPayload): void => undefined;

const onGoToProduct = (_payload: GoToProductPayload): void => undefined;

export const sdk = {
  actions: {setPath, applyVariants, setImageToApply, initCamera},
  events: {
    onAddToCart,
    onGoToProduct,
    onAppliedVariantsChange,
    onVariantSelect,
    onBrandSelect,
    onCameraMirror,
    onExperienceSelect,
    onLookSelect,
    onGoBack,
    onCategorySelect,
    onProductSelect,
    onCurtainSlideEnd,
    onCurtainSlideStart,
    onOpacitySlideStart,
    onOpacitySlideEnd,
    onZoom,
    onCurtainToggle,
    onPathChange,
    onTryNowClick,
    onGdprApprove,
    onUploadPhoto,
    onModelSelect,
    onTakePhotoAgain,
    onUsePhoto,
    onError,
    onReady,
    onTakePhoto,
  },
};
