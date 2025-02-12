import { Page, expect } from "@playwright/test";

const goFromHomePageToRegisterPage = async (page: Page) => {
  await page.goto("/");
  await page.getByText("créer un utilisateur").click();
  await expect(page).toHaveURL("/register");
};

const registerWith = async (page: Page, username: string, password: string) => {
  await page.getByRole("textbox", { name: "username" }).fill(username);
  await page.getByRole("textbox", { name: "password" }).fill(password);
  await page.getByRole("button", { name: "Créer le compte" }).click();
};

export { registerWith, goFromHomePageToRegisterPage };
