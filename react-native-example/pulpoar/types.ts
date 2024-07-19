import type {sdk} from './sdk';

export type Events = typeof sdk.events;

export type EventKeys = keyof typeof sdk.events;

export type Actions = typeof sdk.actions;

export type ActionKeys = keyof typeof sdk.actions;

export type PulpoARProps = {
  plugin: string;
  slug: string;
  actionsRef: React.MutableRefObject<Actions | null>;
} & Partial<Events>;

export type PathChangePayload = {
  path: string;
  referer: string;
};

export type AddToCartPayload = {
  barcode: string | null;
  config: {
    config: any;
    module: string;
  };
  id: string;
  image: string | null;
  name: string;

  product: Product;
  slug: string | null;
  thumbnail_color: string;
  thumbnail_image: string | null;
  translations: [];
  web_link: null;
};

export type AppliedVariantsChangePayload = {
  triggererVariantId: string;
  variants: Variant[];
};

export type VariantsChangePayload = {
  barcode: string | null;
  config: any;
  id: string;
  image: null | string;
  name: string;
  product: Product;
  slug: null | string;
  thumbnail_color: string;
  thumbnail_image: null | string;
  translations: [];
  web_link: null | string;
};

export type BrandSelectPayload = {
  id: string;
  image: string;
  name: string;
  slug: null | string;
  translations: Translation[];
};

export type ModelSelectPayload = {id: number; image: string};

export type ZoomPayload = {type: 'in' | 'out'; value: number};

export type GDPRApprovePayload = {approved: boolean};

export type TryNowClickPayload = {fallback: string};

export type OpacitySlidePayload = {percentage: number};

export type CameraMirrorPayload = {activate: boolean};

export type CategorySelectPayload = {
  id: string;
  image: string;
  name: string;
  slug: string;
  translations: Translation[];
};

export type ProductSelectPayload = {
  brand: Brand;
  category: Category;
  id: string;
  image: string;
  name: string;
  slug: null | string;
  translations: [];
  variants: Variant[];
};

export type CurtainSlidePayload = {
  posX: number;
};

export type CurtainTogglePayload = {
  active: boolean;
};

export type ExperienceSelectPayload = {
  type: string;
};

export type GoToProductPayload = Variant;

export type LookSelectPayload = {
  id: string;
  image: string;
  name: string;
  translations: Translation[];
  variants: Variant[];
};

export type ProjectData = {
  brands: Brand[];
  categories: Category[];
  module: string;
  products: Product[];
  projectId: string;
  themeType: string;
  variants: Variant[];
};

export type Translation = {
  description: string;
  id: number;
  languages_code: string;
  name: string;
  vto_categories_id: string;
};

export type Category = {
  id: string;
  image: string;
  name: string;
  slug: null | string;
  translations: Translation[];
};

export type Brand = {
  id: string;
  image: string;
  name: string;
  slug: null | string;
  translations: Translation[];
};

export type Product = {
  brand: Brand;
  category: Category;
  id: string;
  image: string;
  name: string;
  slug: null | string;
  translations: [];
};

export type Variant = {
  barcode: string | null;
  config: Object;
  id: string;
  image: null | string;
  name: string;
  product: Product;
  slug: null | string;
  thumbnail_color: string;
  thumbnail_image: null | string;
  translations: Translation[];
  web_link: null | string;
};
