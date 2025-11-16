// src/entities/showroom/model/defaultAvatar.ts
import { EquippedItem } from "@/entities/showroom/model/type";

export const defaultEquippedItems: EquippedItem[] = [
  /* --------------------------------------------------------
   * BODY
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "bodies",
    subcategory: "male",
    style: null,
    basePath: "\\spritesheets\\bodies\\male\\{animation}\\light.png",
  },

  /* --------------------------------------------------------
   * HEAD → 기본 얼굴 모양(heads)
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "head",
    subcategory: "heads",
    style: null,
    basePath: "\\spritesheets\\head\\heads\\{animation}\\light.png",
  },

  /* --------------------------------------------------------
   * EYEBROWS → 기본은 thin
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "head",
    subcategory: "eyebrows",
    style: "thin",
    basePath: "\\spritesheets\\head\\eyebrows\\thin\\{animation}\\light_brown.png",
  },

  /* --------------------------------------------------------
   * EYES → 기본은 default_eye
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "head",
    subcategory: "eyes",
    style: "default_eye",
    basePath: "\\spritesheets\\head\\eyes\\default_eye\\{animation}\\brown.png",
  },

  /* --------------------------------------------------------
   * FACES(표정) → 기본 neutral (중요!!!)
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "head",
    subcategory: "faces",
    style: "neutral",
    basePath: "\\spritesheets\\head\\faces\\neutral\\{animation}\\light.png",
  },

  /* --------------------------------------------------------
   * NOSE → style 없음
   * ------------------------------------------------------ */
  {
    equippedId: 0,
    itemId: 0,
    category: "head",
    subcategory: "nose",
    style: null,
    basePath: "\\spritesheets\\head\\nose\\{animation}\\light.png",
  },
];
