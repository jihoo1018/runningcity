// src/entities/showroom/model/defaultAvatar.ts
import { EquippedItem } from "@/entities/showroom/model/type";

export const defaultEquippedItems: EquippedItem[] = [
  {
    equippedId: 0,
    itemId: 1,
    category: "bodies",
    subcategory: "male",
    style: null,
    basePath: "\\spritesheets\\bodies\\male\\{animation}\\light.png",
  },
  {
    equippedId: 0,
    itemId: 706,
    category: "head",
    subcategory: "heads",
    style: null,
    basePath: "\\spritesheets\\head\\heads\\{animation}\\light.png",
  },
  {
    equippedId: 0,
    itemId: 289,
    category: "head",
    subcategory: "eyebrows",
    style: null,
    basePath: "\\spritesheets\\head\\eyebrows\\thin\\{animation}\\light_brown.png",
  },
  {
    equippedId: 0,
    itemId: 579,
    category: "head",
    subcategory: "eyes",
    style: null,
    basePath: "\\spritesheets\\head\\eyes\\\neutral\\{animation}\\brown.png",
  },
  {
    equippedId: 0,
    itemId: 703,
    category: "head",
    subcategory: "nose",
    style: null,
    basePath: "\\spritesheets\\head\\nose\\{animation}\\light.png",
  },
];
