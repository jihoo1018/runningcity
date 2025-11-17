import { StarIcon } from "@/shared/assets/icons";

export function BoutiqueHeader() {
  return (
    <div className="flex items-center justify-center gap-2 pt-2">
      <StarIcon />
      <h1 className="text-title text-custom-white bg-custom-black text-center">부티크</h1>
      <StarIcon className="-scale-x-100" />
    </div>
  );
}
