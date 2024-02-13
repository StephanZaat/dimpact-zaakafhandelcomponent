import { Pipe, PipeTransform } from "@angular/core";
@Pipe({
  name: "capitalizeFirstLetter",
})
export class CapitalizeFirstLetterPipe implements PipeTransform {
  transform(value: string | null): string | null {
    if (typeof value === "string") {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return null;
  }
}
